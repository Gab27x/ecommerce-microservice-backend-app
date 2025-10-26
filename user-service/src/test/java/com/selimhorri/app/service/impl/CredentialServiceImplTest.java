package com.selimhorri.app.service.impl;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.CredentialMappingHelper;
import com.selimhorri.app.repository.CredentialRepository;

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
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("123456789")
                .build();

        credential = Credential.builder()
                .credentialId(1)
                .username("john")
                .password("1234")
                .isEnabled(true)
                .user(user)
                .build();

        credentialDto = CredentialMappingHelper.map(credential);
    }

    @Test
    void findAll_ShouldReturnListOfCredentialDtos() {
        when(credentialRepository.findAll()).thenReturn(List.of(credential));

        List<CredentialDto> result = credentialService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());
        verify(credentialRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCredentialDto_WhenExists() {
        when(credentialRepository.findById(1)).thenReturn(Optional.of(credential));

        CredentialDto result = credentialService.findById(1);

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        verify(credentialRepository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(credentialRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(CredentialNotFoundException.class, () -> credentialService.findById(1));
        verify(credentialRepository, times(1)).findById(1);
    }

    @Test
    void save_ShouldReturnSavedCredentialDto() {
        when(credentialRepository.save(any(Credential.class))).thenReturn(credential);

        CredentialDto result = credentialService.save(credentialDto);

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void update_ShouldReturnUpdatedCredentialDto() {
        when(credentialRepository.save(any(Credential.class))).thenReturn(credential);

        CredentialDto result = credentialService.update(credentialDto);

        assertNotNull(result);
        assertEquals(credentialDto.getUsername(), result.getUsername());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void updateById_ShouldReturnUpdatedCredentialDto_WhenExists() {
        when(credentialRepository.findById(1)).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(credential);

        CredentialDto result = credentialService.update(1, credentialDto);

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        doNothing().when(credentialRepository).deleteById(1);

        credentialService.deleteById(1);

        verify(credentialRepository, times(1)).deleteById(1);
    }

    @Test
    void findByUsername_ShouldReturnCredentialDto_WhenExists() {
        when(credentialRepository.findByUsername("john")).thenReturn(Optional.of(credential));

        CredentialDto result = credentialService.findByUsername("john");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        verify(credentialRepository, times(1)).findByUsername("john");
    }

    @Test
    void findByUsername_ShouldThrowException_WhenNotFound() {
        when(credentialRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> credentialService.findByUsername("john"));
        verify(credentialRepository, times(1)).findByUsername("john");
    }
}
