package com.selimhorri.app.service.impl;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.domain.User;

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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("123456789")
                .build();

        userDto = UserMappingHelper.map(user);
    }

    @Test
    void findAll_ShouldReturnListOfUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(1));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void save_ShouldReturnSavedUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.save(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_ShouldReturnUpdatedUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(userDto);

        assertNotNull(result);
        assertEquals(userDto.getFirstName(), result.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateById_ShouldReturnUpdatedUserDto_WhenUserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1, userDto);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        doNothing().when(userRepository).deleteById(1);

        userService.deleteById(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void findByUsername_ShouldReturnUserDto_WhenExists() {
        when(userRepository.findByCredentialUsername("john")).thenReturn(Optional.of(user));

        UserDto result = userService.findByUsername("john");

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).findByCredentialUsername("john");
    }

    @Test
    void findByUsername_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByCredentialUsername("john")).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> userService.findByUsername("john"));
        verify(userRepository, times(1)).findByCredentialUsername("john");
    }
}
