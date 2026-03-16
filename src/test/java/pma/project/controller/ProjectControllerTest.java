package pma.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pma.project.dto.response.ResponsePermissionDto;
import pma.project.dto.response.ResponseProjectListDto;
import pma.project.dto.response.ResponseVisionScopeDto;
import pma.project.service.ProjectService;
import pma.user.entity.User;
import pma.user.repository.UserRepo;
import org.mockito.Mockito;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(UserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        UserDetails userDetails = Mockito.mock(UserDetails.class);
                        when(userDetails.getUsername()).thenReturn("testuser");
                        return userDetails;
                    }
                })
                .build();
    }

    @Test
    void getMyProjects_ShouldReturnListOfProjects() throws Exception {
        User mockUser = new User();
        User spyUser = Mockito.spy(mockUser);
        when(spyUser.getUserId()).thenReturn(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(spyUser));

        ResponseProjectListDto dto = new ResponseProjectListDto(1, "Test Project", "Desc");
        when(projectService.getProjectsByUserId(1L)).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].projectName").value("Test Project"));
    }

    @Test
    void getContextDiagramUrl_ShouldReturnUrl() throws Exception {
        when(projectService.getContextDiagramUrl(1)).thenReturn("http://diagram.url");

        mockMvc.perform(get("/api/projects/1/context-diagram"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("http://diagram.url"));
    }

    @Test
    void getVisionScopes_ShouldReturnList() throws Exception {
        ResponseVisionScopeDto dto = new ResponseVisionScopeDto(1, "Scope");
        when(projectService.getVisionScopes(1)).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/projects/1/vision-scopes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Scope"));
    }

    @Test
    void getPermissions_ShouldReturnList() throws Exception {
        User mockUser = new User();
        User spyUser = Mockito.spy(mockUser);
        when(spyUser.getUserId()).thenReturn(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(spyUser));

        ResponsePermissionDto dto = new ResponsePermissionDto("CODE_1", "Desc");
        when(projectService.getPermissions(1, 1L)).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/projects/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CODE_1"))
                .andExpect(jsonPath("$[0].description").value("Desc"));
    }
}
