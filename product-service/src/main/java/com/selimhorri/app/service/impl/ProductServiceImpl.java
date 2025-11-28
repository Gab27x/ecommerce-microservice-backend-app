package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.metrics.BusinessMetricsService;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepository;
	private final BusinessMetricsService businessMetricsService;
	
	@Override
	public List<ProductDto> findAll() {
		log.info("*** ProductDto List, service; fetch all products *");
		return this.productRepository.findAll()
				.stream()
					.map(ProductMappingHelper::map)
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
	
	@Override
	public ProductDto findById(final Integer productId) {
		log.info("*** ProductDto, service; fetch product by id *");
		return this.productRepository.findById(productId)
				.map(ProductMappingHelper::map)
				.orElseThrow(() -> new ProductNotFoundException(String.format("Product with id: %d not found", productId)));
	}
	
	@Override
	public ProductDto save(final ProductDto productDto) {
		log.info("*** ProductDto, service; save product *");
		ProductDto savedProduct = ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
		// Registrar métrica de negocio: producto creado
		businessMetricsService.recordProductCreated();
		// Actualizar gauge de total de productos
		businessMetricsService.updateTotalProductsGauge(this.productRepository.count());
		return savedProduct;
	}
	
	@Override
	public ProductDto update(final ProductDto productDto) {
		log.info("*** ProductDto, service; update product *");
		ProductDto updatedProduct = ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
		// Registrar métrica de negocio: producto actualizado
		businessMetricsService.recordProductUpdated();
		return updatedProduct;
	}
	
	@Override
	public ProductDto update(final Integer productId, final ProductDto productDto) {
		log.info("*** ProductDto, service; update product with productId *");
		ProductDto updatedProduct = ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(this.findById(productId))));
		// Registrar métrica de negocio: producto actualizado
		businessMetricsService.recordProductUpdated();
		return updatedProduct;
	}
	
	@Override
	public void deleteById(final Integer productId) {
		log.info("*** Void, service; delete product by id *");
		this.productRepository.delete(ProductMappingHelper
				.map(this.findById(productId)));
		// Registrar métrica de negocio: producto eliminado
		businessMetricsService.recordProductDeleted();
		// Actualizar gauge de total de productos
		businessMetricsService.updateTotalProductsGauge(this.productRepository.count());
	}
	
	
	
}









