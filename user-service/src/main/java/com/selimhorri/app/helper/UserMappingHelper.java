package com.selimhorri.app.helper;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;

public interface UserMappingHelper {
	
	static UserDto map(final User user) {
		if (user == null) {
			return null;
		}
		
		CredentialDto credentialDto = null;
		if (user.getCredential() != null) {
			credentialDto = CredentialDto.builder()
					.credentialId(user.getCredential().getCredentialId())
					.username(user.getCredential().getUsername())
					.password(user.getCredential().getPassword())
					.roleBasedAuthority(user.getCredential().getRoleBasedAuthority())
					.isEnabled(user.getCredential().getIsEnabled())
					.isAccountNonExpired(user.getCredential().getIsAccountNonExpired())
					.isAccountNonLocked(user.getCredential().getIsAccountNonLocked())
					.isCredentialsNonExpired(user.getCredential().getIsCredentialsNonExpired())
					.build();
		}
		
		Set<AddressDto> addressDtos = null;
		if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
			addressDtos = user.getAddresses().stream()
					.map(AddressMappingHelper::mapWithoutUser)
					.collect(Collectors.toSet());
		}
		
		return UserDto.builder()
				.userId(user.getUserId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.imageUrl(user.getImageUrl())
				.email(user.getEmail())
				.phone(user.getPhone())
				.credentialDto(credentialDto)
				.addressDtos(addressDtos)
				.build();
	}
	
	public static User map(final UserDto userDto) {
		if (userDto == null) {
			return null;
		}
		
		User user = User.builder()
				.userId(userDto.getUserId())
				.firstName(userDto.getFirstName())
				.lastName(userDto.getLastName())
				.imageUrl(userDto.getImageUrl())
				.email(userDto.getEmail())
				.phone(userDto.getPhone())
				.build();
		
		// Establecer relación bidireccional con Credential
		if (userDto.getCredentialDto() != null) {
			Credential credential = Credential.builder()
					.credentialId(userDto.getCredentialDto().getCredentialId())
					.username(userDto.getCredentialDto().getUsername())
					.password(userDto.getCredentialDto().getPassword())
					.roleBasedAuthority(userDto.getCredentialDto().getRoleBasedAuthority())
					.isEnabled(userDto.getCredentialDto().getIsEnabled())
					.isAccountNonExpired(userDto.getCredentialDto().getIsAccountNonExpired())
					.isAccountNonLocked(userDto.getCredentialDto().getIsAccountNonLocked())
					.isCredentialsNonExpired(userDto.getCredentialDto().getIsCredentialsNonExpired())
					.user(user) // IMPORTANTE: Establecer relación inversa
					.build();
			user.setCredential(credential);
		}
		
		// Establecer relación bidireccional con Addresses
		if (userDto.getAddressDtos() != null && !userDto.getAddressDtos().isEmpty()) {
			Set<Address> addresses = new HashSet<>();
			for (AddressDto addressDto : userDto.getAddressDtos()) {
				Address address = Address.builder()
						.addressId(addressDto.getAddressId())
						.fullAddress(addressDto.getFullAddress())
						.postalCode(addressDto.getPostalCode())
						.city(addressDto.getCity())
						.user(user) // IMPORTANTE: Establecer relación inversa
						.build();
				addresses.add(address);
			}
			user.setAddresses(addresses);
		}
		
		return user;
	}
	
}