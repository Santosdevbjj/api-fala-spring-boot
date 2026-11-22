package br.com.ciandt.budget.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ciandt.budget.application.CreateTransactionUseCase;
import br.com.ciandt.budget.application.ListTransactionsUseCase;
import br.com.ciandt.budget.application.TransactionTools;
import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssistantControllerTest {

    @Test
    void shouldRejectBlankMessageOnTextEndpoint() throws Exception {
        AssistantController controller = buildController("Resposta de teste", new byte[]{1, 2, 3}, "texto");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/assistant/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A mensagem é obrigatória."));
    }

    @Test
    void shouldProcessTextCommandAndReturnAnswer() throws Exception {
        AssistantController controller = buildController(
                "Despesa de R$ 60,00 registrada em estacionamento na categoria AUTO.",
                new byte[]{1, 2, 3},
                "texto"
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/assistant/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Paguei R$ 60 de estacionamento hoje"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer")
                        .value("Despesa de R$ 60,00 registrada em estacionamento na categoria AUTO."));
    }

    @Test
    void shouldTranscribeAudioAndReturnMp3ResponseOnVoiceEndpoint() throws Exception {
        byte[] fakeMp3 = "FAKE-MP3-BYTES".getBytes(StandardCharsets.UTF_8);
        AssistantController controller = buildController(
                "Registrei R$ 60,00 em estacionamento na categoria AUTO.",
                fakeMp3,
                "Paguei 60 reais de estacionamento"
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "comando.wav",
                "audio/wav",
                "RIFF-FAKE-WAV".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/assistant/voice").file(audioFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assistant-response.mp3\""))
                .andExpect(content().bytes(fakeMp3));
    }

    private AssistantController buildController(String chatAnswer, byte[] mp3Bytes, String transcriptText) {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(chatAnswer);

        TranscriptionModel transcriptionModel = mock(TranscriptionModel.class);
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn(transcriptText);

        TextToSpeechModel speechModel = mock(TextToSpeechModel.class);
        when(speechModel.call(anyString())).thenReturn(mp3Bytes);

        TransactionRepository repository = new StubRepository();
        TransactionTools tools = new TransactionTools(
                new CreateTransactionUseCase(repository),
                new ListTransactionsUseCase(repository)
        );

        return new AssistantController(builder, tools, transcriptionModel, speechModel);
    }

    private static final class StubRepository implements TransactionRepository {
        @Override
        public Transaction save(Transaction transaction) {
            return transaction;
        }

        @Override
        public List<Transaction> findAll() {
            return List.of();
        }

        @Override
        public List<Transaction> findByCategory(Category category) {
            return List.of();
        }

        @Override
        public Optional<Transaction> findById(UUID id) {
            return Optional.empty();
        }
    }
}
