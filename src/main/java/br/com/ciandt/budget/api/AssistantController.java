package br.com.ciandt.budget.api;

import br.com.ciandt.budget.application.TransactionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/** Orquestra entrada por texto/voz, ferramentas de negócio e resposta sintetizada. */
@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {
    private static final String SYSTEM_PROMPT = """
        Você é um assistente financeiro pessoal que conversa em português do Brasil.
        Ajude a registrar e consultar despesas usando somente as ferramentas disponíveis.
        Nunca invente transações ou confirme um registro sem a ferramenta retornar sucesso.
        Categorias: GROCERIES (mercado e alimentação), FARMA (farmácia e saúde), AUTO (carro, transporte e estacionamento), OTHER (demais despesas).
        Converta reais para centavos inteiros: R$ 60,00 corresponde a 6000 centavos.
        Se faltar valor, descrição ou informação essencial, faça uma pergunta em vez de criar uma despesa.
        Responda de forma breve e clara.
        """;

    private final ChatClient chatClient;
    private final TransactionTools tools;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel speechModel;

    public AssistantController(ChatClient.Builder builder, TransactionTools tools,
                               TranscriptionModel transcriptionModel, TextToSpeechModel speechModel) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
        this.tools = tools;
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    @PostMapping("/text")
    public AssistantTextResponse text(@RequestBody AssistantTextRequest request) {
        if (request.message() == null || request.message().isBlank()) throw new IllegalArgumentException("A mensagem é obrigatória.");
        String answer = chatClient.prompt().user(request.message()).tools(tools).call().content();
        return new AssistantTextResponse(answer);
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> voice(@RequestPart("audio") MultipartFile audio) throws IOException {
        if (audio.isEmpty()) throw new IllegalArgumentException("Envie um arquivo de áudio não vazio no campo 'audio'.");
        String transcript = transcriptionModel.transcribe(new ByteArrayResource(audio.getBytes()) {
            @Override public String getFilename() { return audio.getOriginalFilename() == null ? "voice.wav" : audio.getOriginalFilename(); }
        });
        String answer = chatClient.prompt().user(transcript).tools(tools).call().content();
        byte[] mp3 = speechModel.call(answer);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assistant-response.mp3\"")
                .body(mp3);
    }

    public record AssistantTextRequest(String message) { }
    public record AssistantTextResponse(String answer) { }
}
