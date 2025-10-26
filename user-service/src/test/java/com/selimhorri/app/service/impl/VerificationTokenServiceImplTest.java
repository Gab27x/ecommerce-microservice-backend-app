package com.selimhorri.app.service.impl;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.VerificationToken;
import com.selimhorri.app.dto.VerificationTokenDto;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;
import com.selimhorri.app.helper.VerificationTokenMappingHelper;
import com.selimhorri.app.repository.VerificationTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    private VerificationToken verificationToken;
    private VerificationTokenDto verificationTokenDto;

    @BeforeEach
    void setUp() {
        Credential credential = Credential.builder()
                .credentialId(1)
                .username("john")
                .password("1234")
                .isEnabled(true)
                .build();

        verificationToken = VerificationToken.builder()
                .verificationTokenId(100)
                .token("abcd1234")
                .expireDate(LocalDate.now().plusDays(1))
                .credential(credential)
                .build();

        verificationTokenDto = VerificationTokenMappingHelper.map(verificationToken);
    }

    @Test
    void findAll_ShouldReturnListOfVerificationTokenDtos() {
        when(verificationTokenRepository.findAll()).thenReturn(List.of(verificationToken));

        List<VerificationTokenDto> result = verificationTokenService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("abcd1234", result.get(0).getToken());
        verify(verificationTokenRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnVerificationTokenDto_WhenExists() {
        when(verificationTokenRepository.findById(100)).thenReturn(Optional.of(verificationToken));

        VerificationTokenDto result = verificationTokenService.findById(100);

        assertNotNull(result);
        assertEquals("abcd1234", result.getToken());
        verify(verificationTokenRepository, times(1)).findById(100);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(verificationTokenRepository.findById(100)).thenReturn(Optional.empty());

        assertThrows(VerificationTokenNotFoundException.class, () -> verificationTokenService.findById(100));
        verify(verificationTokenRepository, times(1)).findById(100);
    }

    @Test
    void save_ShouldReturnSavedVerificationTokenDto() {
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        VerificationTokenDto result = verificationTokenService.save(verificationTokenDto);

        assertNotNull(result);
        assertEquals("abcd1234", result.getToken());
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void update_ShouldReturnUpdatedVerificationTokenDto() {
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        VerificationTokenDto result = verificationTokenService.update(verificationTokenDto);

        assertNotNull(result);
        assertEquals(verificationTokenDto.getToken(), result.getToken());
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void updateById_ShouldReturnUpdatedVerificationTokenDto_WhenExists() {
        when(verificationTokenRepository.findById(100)).thenReturn(Optional.of(verificationToken));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        VerificationTokenDto result = verificationTokenService.update(100, verificationTokenDto);

        assertNotNull(result);
        assertEquals("abcd1234", result.getToken());
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        doNothing().when(verificationTokenRepository).deleteById(100);

        verificationTokenService.deleteById(100);

        verify(verificationTokenRepository, times(1)).deleteById(100);
    }
}
