package com.escravosdev.bots.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VerificationAiService {

    @Value("${nvidia.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    private static final String NVIDIA_API = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL      = "google/gemma-3n-e4b-it";

    public record AiAnalysis(boolean problematico, String resumo, String sugestao) {}

    public AiAnalysis analisarResposta1(String resposta) {
        var prompt = """
                Você é um assistente moderador de um servidor Discord católico.
                Analise a seguinte resposta de um candidato à entrada no servidor.
                
                A resposta é para as seguintes perguntas:
                1. Qual é a sua religião? (se católico, é recém-convertido ou desde sempre? Reconhece o Papa?)
                2. Caso discorde de algum ensinamento da Igreja, como costuma agir?
                3. Já leu o Catecismo ou documentos oficiais?
                4. Qual é o seu sexo?
                5. Por que deseja participar?
                6. Onde encontrou o servidor?
                
                RESPOSTA DO CANDIDATO:
                %s
                
                Avalie se há sinais PROBLEMÁTICOS como:
                - Sedevacantismo explícito (nega autoridade do Papa atual)
                - Hostilidade à Igreja Católica
                - Má-fé evidente ou provocação
                - Respostas completamente vazias ou sem sentido
                
                Responda APENAS em JSON válido, sem texto antes ou depois:
                {"problematico": true/false, "resumo": "...", "sugestao": "..."}
                """.formatted(resposta);

        return chamar(prompt);
    }

    public AiAnalysis analisarResposta2(String resposta) {
        var prompt = """
                Você é um assistente moderador de um servidor Discord católico.
                O candidato acabou de responder se concorda com as regras do servidor.
                
                As regras proíbem: pornografia, ataques à Igreja/Papa/Magistério,
                sedevacantismo, militância político-partidária, teorias conspiratórias,
                desordem deliberada ou provocações.
                
                RESPOSTA DO CANDIDATO:
                %s
                
                Avalie se há sinais PROBLEMÁTICOS como:
                - Recusa explícita de alguma regra
                - Ironia ou sarcasmo sobre as regras
                - Condicionamento ("concordo, mas...")
                - Resposta incompreensível ou sem sentido
                
                Responda APENAS em JSON válido, sem texto antes ou depois:
                {"problematico": true/false, "resumo": "...", "sugestao": "..."}
                """.formatted(resposta);

        return chamar(prompt);
    }

    public String gerarBriefingFinal(VerificationSession session) {
        var pings = new StringBuilder();
        if (session.isPingOracoes()) pings.append("Orações, ");
        if (session.isPingBump())    pings.append("Bump, ");
        if (session.isPingReviver()) pings.append("Reviver Chat, ");

        var prompt = """
                Você é um assistente moderador de um servidor Discord católico.
                Gere um briefing objetivo para os administradores avaliarem um candidato.
                
                RESPOSTA 1 (apresentação, religião, postura):
                %s
                
                ANÁLISE R1: %s
                
                RESPOSTA 2 (aceite das regras):
                %s
                
                ANÁLISE R2: %s
                
                PINGS SOLICITADOS: %s
                
                Gere um briefing em português com:
                1. Perfil resumido (religião, postura, motivação)
                2. Pontos positivos
                3. Pontos de atenção (se houver)
                4. Recomendação: ACEITAR / RECUSAR / OBSERVAR
                
                Máximo 300 palavras.
                """.formatted(
                session.getResposta1(),
                session.getAnaliseR1(),
                session.getResposta2(),
                session.getAnaliseR2(),
                pings.isEmpty() ? "Nenhum" : pings.toString()
        );

        try {
            return chamarTexto(prompt);
        } catch (Exception e) {
            log.error("Erro ao gerar briefing", e);
            return "⚠️ Erro ao gerar briefing automático. Avalie manualmente.";
        }
    }

    private AiAnalysis chamar(String prompt) {
        try {
            var text = chamarTexto(prompt);
            var clean = text.strip();

            // extrai o JSON — modelo pode colocar texto antes/depois
            var start = clean.indexOf('{');
            var end   = clean.lastIndexOf('}');
            if (start >= 0 && end > start) {
                clean = clean.substring(start, end + 1);
            }

            var problematico = clean.contains("\"problematico\": true") ||
                    clean.contains("\"problematico\":true");
            var resumo   = extrairCampo(clean, "resumo");
            var sugestao = extrairCampo(clean, "sugestao");

            return new AiAnalysis(problematico, resumo, sugestao);

        } catch (Exception e) {
            log.error("Erro ao chamar NVIDIA AI", e);
            return new AiAnalysis(false, "Erro na análise automática.", "Avalie manualmente.");
        }
    }

    private String chamarTexto(String prompt) {
        var payload = Map.of(
                "model",             MODEL,
                "messages",          List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens",        512,
                "temperature",       0.20,
                "top_p",             0.70,
                "frequency_penalty", 0.00,
                "presence_penalty",  0.00,
                "stream",            false
        );

        var response = restClient.post()
                .uri(NVIDIA_API)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .body(Map.class);

        var choices = (List<Map<String, Object>>) response.get("choices");
        var message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String extrairCampo(String json, String campo) {
        try {
            var key   = "\"" + campo + "\": \"";
            var start = json.indexOf(key) + key.length();
            var end   = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
