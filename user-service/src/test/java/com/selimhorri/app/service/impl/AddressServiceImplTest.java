package com.selimhorri.app.service.impl;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.helper.AddressMappingHelper;
import com.selimhorri.app.repository.AddressRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address address;
    private AddressDto addressDto;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("123456789")
                .build();

        address = Address.builder()
                .addressId(10)
                .fullAddress("123 Main St")
                .postalCode("10001")
                .city("New York")
                .user(user)
                .build();

        addressDto = AddressMappingHelper.map(address);
    }

    @Test
    void findAll_ShouldReturnListOfAddressDtos() {
        when(addressRepository.findAll()).thenReturn(List.of(address));

        List<AddressDto> result = addressService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("123 Main St", result.get(0).getFullAddress());
        verify(addressRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnAddressDto_WhenExists() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));

        AddressDto result = addressService.findById(10);

        assertNotNull(result);
        assertEquals("123 Main St", result.getFullAddress());
        verify(addressRepository, times(1)).findById(10);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(addressRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.findById(10));
        verify(addressRepository, times(1)).findById(10);
    }

    @Test
    void save_ShouldReturnSavedAddressDto() {
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.save(addressDto);

        assertNotNull(result);
        assertEquals(addressDto.getCity(), result.getCity());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void update_ShouldReturnUpdatedAddressDto() {
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.update(addressDto);

        assertNotNull(result);
        assertEquals(addressDto.getPostalCode(), result.getPostalCode());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateById_ShouldReturnUpdatedAddressDto_WhenExists() {
        when(addressRepository.findById(10)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.update(10, addressDto);

        assertNotNull(result);
        assertEquals("123 Main St", result.getFullAddress());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        doNothing().when(addressRepository).deleteById(10);

        addressService.deleteById(10);

        verify(addressRepository, times(1)).deleteById(10);
    }
}
