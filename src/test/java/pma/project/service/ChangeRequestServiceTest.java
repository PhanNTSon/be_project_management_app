package pma.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pma.project.dto.request.ChangeItemSubmitDto;
import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.entity.change.ChangeItem;
import pma.project.entity.change.ChangeRequest;
import pma.project.entity.core.Project;
import pma.project.repository.ChangeItemRepository;
import pma.project.repository.ChangeRequestRepository;
import pma.project.repository.ProjectRepository;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
class ChangeRequestServiceTest {

    @Mock
    private ChangeRequestRepository changeRequestRepository;
    @Mock
    private ChangeItemRepository changeItemRepository;
    @Mock
    private ProjectPermissionService projectPermissionService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private ChangeRequestService changeRequestService;

    private Project mockProject;
    private User mockUser;
    private ChangeRequestSubmitDto mockSubmitDto;
    private ChangeRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockProject = new Project();
        // Giả lập mockProject có id (cần reflection hoặc mock getProjectId)
        
        mockUser = new User();
        // mockUser có id

        ChangeItemSubmitDto itemDto = new ChangeItemSubmitDto("USECASE", 1, "UPDATE", "title", "old", "new");
        mockSubmitDto = new ChangeRequestSubmitDto("Test Title", "Desc", List.of(itemDto));

        mockRequest = new ChangeRequest(mockProject, mockUser, "Test Title", "Desc");
    }

    @Test
    @DisplayName("EDITOR creates a Change request -> PENDING")
    void testCreateChangeRequest_Editor_Pending() {
        // Arrange
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(mockProject));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(changeRequestRepository.save(any(ChangeRequest.class))).thenReturn(mockRequest);
        when(projectPermissionService.hasRole(anyLong(), anyInt(), eq("MAINTAINER"))).thenReturn(false);

        // Act
        ChangeRequest result = changeRequestService.createChangeRequest(1L, 1, mockSubmitDto);

        // Assert
        assertEquals("PENDING", result.getStatus());
        verify(projectPermissionService, times(1)).validatePermission(anyLong(), anyInt(), eq("PROJECT_EDIT"));
        verify(changeItemRepository, times(1)).save(any(ChangeItem.class));
        verify(changeRequestRepository, times(1)).save(any(ChangeRequest.class));
    }

    @Test
    @DisplayName("MAINTAINER creates a Change request -> Auto APPROVED")
    void testCreateChangeRequest_Maintainer_Approved() {
        // Arrange
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(mockProject));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        
        // Cần đảm bảo mockProject không ném NPE khi log/trỏ vào getProjectId(). 
        // Trong java, mockProject = new Project() thì getProjectId() mặc định là null.
        // Giải pháp nhanh cho test: dùng reflection hoặc tạo project với ID, hay đơn giản dùng thay đổi đối tượng
        try {
            java.lang.reflect.Field idField = Project.class.getDeclaredField("projectId");
            idField.setAccessible(true);
            idField.set(mockProject, 1);
        } catch (Exception e) {}
        
        ChangeRequest pendingReq = new ChangeRequest(mockProject, mockUser, "Test Title", "Desc");
        // Giả lập sau khi save lần 1
        when(changeRequestRepository.save(any(ChangeRequest.class))).thenReturn(pendingReq);
        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(pendingReq)); // Dùng cho hàm applyChangeRequest

        when(projectPermissionService.hasRole(anyLong(), anyInt(), eq("MAINTAINER"))).thenReturn(true);

        // Act
        ChangeRequest result = changeRequestService.createChangeRequest(1L, 1, mockSubmitDto);

        // Assert
        assertEquals("APPROVED", result.getStatus());
        verify(projectPermissionService, times(1)).validatePermission(anyLong(), anyInt(), eq("PROJECT_EDIT"));
        verify(projectPermissionService, times(1)).validatePermission(anyLong(), anyInt(), eq("APPROVE_CHANGES"));
        // Lần 1 lưu PENDING, Lần 2 lưu APPROVED
        verify(changeRequestRepository, atLeast(2)).save(any(ChangeRequest.class));
    }
}
