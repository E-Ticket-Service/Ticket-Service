package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.exception.DuplicateEventException;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.EventMapper;
import abb.tech.ticket_service.model.Event;
import abb.tech.ticket_service.repository.EventRepository;
import abb.tech.ticket_service.service.serviceImpl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private static final Long EVENT_ID = 1L;
    private static final String EVENT_NAME = "Jazz Night";

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(EVENT_ID);
        event.setName(EVENT_NAME);
    }

    @Nested
    @DisplayName("Tədbir Yaratma Testləri")
    class CreationTests {

        @Test
        @DisplayName("Uğurlu — Yeni tədbir yaradılmalıdır")
        void createEvent_success() {
            ReqEventDto reqDto = mock(ReqEventDto.class);
            RespEventDto respDto = mock(RespEventDto.class);

            when(reqDto.name()).thenReturn(EVENT_NAME);
            when(eventRepository.existsByName(EVENT_NAME)).thenReturn(false);
            when(eventMapper.toEntity(reqDto)).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(respDto);

            RespEventDto result = eventService.createEvent(reqDto);

            assertNotNull(result);
            verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("Xəta — Eyni adlı tədbir olduqda DuplicateEventException atmalıdır")
        void createEvent_duplicateName() {
            ReqEventDto reqDto = mock(ReqEventDto.class);
            when(reqDto.name()).thenReturn(EVENT_NAME);
            when(eventRepository.existsByName(EVENT_NAME)).thenReturn(true);

            assertThrows(DuplicateEventException.class, () -> eventService.createEvent(reqDto));
            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tədbir Axtarışı Testləri")
    class RetrievalTests {

        @Test
        @DisplayName("Uğurlu — ID-yə görə tədbir tapılmalıdır")
        void getEventById_success() {
            RespEventDto respDto = mock(RespEventDto.class);
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventMapper.toResponse(event)).thenReturn(respDto);

            RespEventDto result = eventService.getEventById(EVENT_ID);

            assertNotNull(result);
            verify(eventRepository).findById(EVENT_ID);
        }

        @Test
        @DisplayName("Xəta — Tapılmadıqda NotFoundException atmalıdır")
        void getEventById_notFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> eventService.getEventById(EVENT_ID));
        }

        @Test
        @DisplayName("Uğurlu — Bütün tədbirlər siyahı kimi qaytarılmalıdır")
        void getAllEvents_success() {
            List<Event> events = List.of(event);
            when(eventRepository.findAll()).thenReturn(events);
            when(eventMapper.toDoList(events)).thenReturn(List.of(mock(RespEventDto.class)));

            List<RespEventDto> results = eventService.getAllEvents();

            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("Yeniləmə və Silmə Testləri")
    class ModificationTests {

        @Test
        @DisplayName("Uğurlu — Tədbir yenilənməlidir")
        void updateEvent_success() {
            ReqEventDto reqDto = mock(ReqEventDto.class);
            RespEventDto respDto = mock(RespEventDto.class);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventMapper.toResponse(event)).thenReturn(respDto);

            RespEventDto result = eventService.updateEvent(EVENT_ID, reqDto);

            assertNotNull(result);
            verify(eventMapper).updateEntityFromDto(reqDto, event);
            verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("Uğurlu — Tədbir silinməlidir")
        void deleteEvent_success() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

            eventService.deleteEvent(EVENT_ID);

            verify(eventRepository).deleteById(EVENT_ID);
        }
    }
}