package com.selimhorri.app.helper;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.dto.UserDto;

public interface AddressMappingHelper {
	
	static AddressDto map(final Address address) {
		if (address == null) {
			return null;
		}
		
		UserDto userDto = null;
		if (address.getUser() != null) {
			userDto = UserDto.builder()
					.userId(address.getUser().getUserId())
					.firstName(address.getUser().getFirstName())
					.lastName(address.getUser().getLastName())
					.imageUrl(address.getUser().getImageUrl())
					.email(address.getUser().getEmail())
					.phone(address.getUser().getPhone())
					.build();
		}
		
		return AddressDto.builder()
				.addressId(address.getAddressId())
				.fullAddress(address.getFullAddress())
				.postalCode(address.getPostalCode())
				.city(address.getCity())
				.userDto(userDto)
				.build();
	}
	
	// Método para evitar referencia circular al mapear desde User
	static AddressDto mapWithoutUser(final Address address) {
		if (address == null) {
			return null;
		}
		
		return AddressDto.builder()
				.addressId(address.getAddressId())
				.fullAddress(address.getFullAddress())
				.postalCode(address.getPostalCode())
				.city(address.getCity())
				.build();
	}
	
	public static Address map(final AddressDto addressDto) {
		if (addressDto == null) {
			return null;
		}
		
		Address address = Address.builder()
				.addressId(addressDto.getAddressId())
				.fullAddress(addressDto.getFullAddress())
				.postalCode(addressDto.getPostalCode())
				.city(addressDto.getCity())
				.build();
		
		// Establecer relación bidireccional con User
		if (addressDto.getUserDto() != null) {
			User user = User.builder()
					.userId(addressDto.getUserDto().getUserId())
					.firstName(addressDto.getUserDto().getFirstName())
					.lastName(addressDto.getUserDto().getLastName())
					.imageUrl(addressDto.getUserDto().getImageUrl())
					.email(addressDto.getUserDto().getEmail())
					.phone(addressDto.getUserDto().getPhone())
					.build();
			address.setUser(user);
		}
		
		return address;
	}
	
}