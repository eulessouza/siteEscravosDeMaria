package com.escravosdev.bots.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.escravosdev.bots.verification.VerificationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    @Lazy
    private final JDA jda;
    private final VerificationAiService aiService;

    // ticketChannelId → session
    private final Map<String, VerificationSession> sessions = new ConcurrentHashMap<>();

    // ── Iniciar sessão ───────────────────────────────────────────────────────

    public void iniciarVerificacao(String ticketChannelId, Member member) {
        if (sessions.containsKey(ticketChannelId)) return;

        var session = new VerificationSession(
                member.getId(),
                ticketChannelId,
                member.getEffectiveName()
        );
        sessions.put(ticketChannelId, session);

        var channel = jda.getTextChannelById(ticketChannelId);
        if (channel == null) return;

        channel.sendMessage(PERGUNTA_1.formatted(member.getAsMention())).queue();
        log.info("Verificação iniciada para {} no canal {}", member.getEffectiveName(), ticketChannelId);
    }

    // ── Processar mensagem ───────────────────────────────────────────────────

    public void processarMensagem(String ticketChannelId, String userId, String conteudo) {
        var session = sessions.get(ticketChannelId);
        if (session == null || !session.getUserId().equals(userId)) return;
        if (session.getStep() == VerificationSession.Step.PAUSADO ||
                session.getStep() == VerificationSession.Step.CONCLUIDO) return;

        session.updateActivity();

        switch (session.getStep()) {
            case AGUARDANDO_R1 -> processarR1(session, conteudo, ticketChannelId);
            case AGUARDANDO_R2 -> processarR2(session, conteudo, ticketChannelId);
            default -> {}
        }
    }

    private void processarR1(VerificationSession session, String resposta, String channelId) {
        session.setResposta1(resposta);

        var channel = jda.getTextChannelById(channelId);
        if (channel == null) return;

        channel.sendMessage("_Analisando sua resposta..._").queue(msg -> {
            var analise = aiService.analisarResposta1(resposta);
            session.setAnaliseR1(analise.resumo() + " | " + analise.sugestao());

            msg.delete().queue();

            if (analise.problematico()) {
                session.setStep(VerificationSession.Step.PAUSADO);
                channel.sendMessage("""
                        ⏸️ Sua verificação foi pausada temporariamente.
                        Um administrador irá analisá-la em breve. Por favor, aguarde.
                        """).queue();
                notificarAdmProblema(session, "Resposta 1", resposta, analise.resumo());
            } else {
                session.setStep(VerificationSession.Step.AGUARDANDO_R2);
                channel.sendMessage(PERGUNTA_2).queue();
            }
        });
    }

    private void processarR2(VerificationSession session, String resposta, String channelId) {
        session.setResposta2(resposta);

        var channel = jda.getTextChannelById(channelId);
        if (channel == null) return;

        channel.sendMessage("_Analisando sua resposta..._").queue(msg -> {
            var analise = aiService.analisarResposta2(resposta);
            session.setAnaliseR2(analise.resumo() + " | " + analise.sugestao());

            msg.delete().queue();

            if (analise.problematico()) {
                session.setStep(VerificationSession.Step.PAUSADO);
                channel.sendMessage("""
                        ⏸️ Sua verificação foi pausada temporariamente.
                        Um administrador irá analisá-la em breve. Por favor, aguarde.
                        """).queue();
                notificarAdmProblema(session, "Resposta 2", resposta, analise.resumo());
            } else {
                session.setStep(VerificationSession.Step.AGUARDANDO_R3);
                enviarPergunta3(channel);
            }
        });
    }

    private void enviarPergunta3(TextChannel channel) {
        var menu = StringSelectMenu.create("pings")
                .setPlaceholder("Selecione os cargos de ping que deseja (opcional)")
                .setMinValues(0)
                .setMaxValues(3)
                .addOption("🙏 Avisos de Oração", "oracoes", "Notifica os terços e eventos de orações")
                .addOption("🔔 Lembrete de Bump", "bump", "Lembrar de dar o comando pro bot")
                .addOption("💬 Incentivo ao Chat", "reviver", "Lembrar de mandar mensagem no chat")
                .build();

        channel.sendMessage(PERGUNTA_3)
                .addActionRow(menu)
                .addActionRow(
                        Button.success("pings_confirmar", "Confirmar seleção"),
                        Button.secondary("pings_nenhum", "Nenhum")
                )
                .queue();
    }

    // ── Processar seleção de pings ───────────────────────────────────────────

    public void processarSelecaoPings(String ticketChannelId, String userId,
                                      List<String> selecionados) {
        var session = sessions.get(ticketChannelId);
        if (session == null || !session.getUserId().equals(userId)) return;

        session.setPingOracoes(selecionados.contains("oracoes"));
        session.setPingBump(selecionados.contains("bump"));
        session.setPingReviver(selecionados.contains("reviver"));
    }

    public void confirmarPings(String ticketChannelId, String userId) {
        var session = sessions.get(ticketChannelId);
        if (session == null || !session.getUserId().equals(userId)) return;
        if (session.getStep() != VerificationSession.Step.AGUARDANDO_R3) return;

        session.setStep(VerificationSession.Step.CONCLUIDO);

        var channel = jda.getTextChannelById(ticketChannelId);
        if (channel != null) {
            channel.sendMessage("""
                    ✅ Respostas recebidas! Sua verificação está sendo avaliada pelos administradores.
                    Você será notificado em breve. 🙏
                    """).queue();
        }

        enviarBriefingAdm(session);
    }

    // ── Briefing para ADM ────────────────────────────────────────────────────

    private void enviarBriefingAdm(VerificationSession session) {
        var canal = jda.getTextChannelById(CANAL_ADM);
        if (canal == null) {
            log.error("Canal ADM não encontrado: {}", CANAL_ADM);
            return;
        }

        var briefing = aiService.gerarBriefingFinal(session);
        session.setBriefingFinal(briefing);

        var pings = new ArrayList<String>();
        if (session.isPingOracoes()) pings.add("🙏 Orações");
        if (session.isPingBump())    pings.add("🔔 Bump");
        if (session.isPingReviver()) pings.add("💬 Reviver Chat");

        var embed = new EmbedBuilder()
                .setTitle("📋 Verificação — " + session.getUsername())
                .setColor(new Color(0x5865F2))
                .setTimestamp(Instant.now())
                .addField("👤 Usuário", "<@" + session.getUserId() + ">", true)
                .addField("🎫 Ticket", "<#" + session.getTicketChannelId() + ">", true)
                .addField("📌 Pings solicitados",
                        pings.isEmpty() ? "Nenhum" : String.join(", ", pings), false)
                .addField("🤖 Análise R1", session.getAnaliseR1(), false)
                .addField("🤖 Análise R2", session.getAnaliseR2(), false)
                .addField("📝 Briefing Final", briefing.length() > 1000
                        ? briefing.substring(0, 997) + "..." : briefing, false)
                .build();

        canal.sendMessageEmbeds(embed)
                .addActionRow(
                        Button.success("aceitar_" + session.getUserId(), "✅ Aceitar"),
                        Button.danger("recusar_" + session.getUserId(), "❌ Recusar")
                )
                .queue();
    }

    private void notificarAdmProblema(VerificationSession session, String etapa,
                                      String resposta, String analise) {
        var canal = jda.getTextChannelById(CANAL_ADM);
        if (canal == null) return;

        var embed = new EmbedBuilder()
                .setTitle("⚠️ Verificação Pausada — " + session.getUsername())
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now())
                .addField("👤 Usuário", "<@" + session.getUserId() + ">", true)
                .addField("🎫 Ticket", "<#" + session.getTicketChannelId() + ">", true)
                .addField("⚠️ Etapa problemática", etapa, false)
                .addField("💬 Resposta do candidato",
                        resposta.length() > 500 ? resposta.substring(0, 497) + "..." : resposta, false)
                .addField("🤖 Análise da IA", analise, false)
                .build();

        canal.sendMessageEmbeds(embed)
                .addActionRow(
                        Button.success("retomar_" + session.getUserId(), "▶️ Retomar verificação"),
                        Button.danger("recusar_" + session.getUserId(), "❌ Recusar e Kickar")
                )
                .queue();
    }

    // ── Aceitar / Recusar ────────────────────────────────────────────────────

    public void aceitarCandidato(String userId, String admId) {
        var session = buscarSessionPorUser(userId);
        if (session == null) return;

        var guild = jda.getGuilds().get(0);
        guild.retrieveMemberById(userId).queue(member -> {

            // remove Plebeu e Meeiro
            List.of(ROLE_PLEBEU, ROLE_MEEIRO).forEach(roleId -> {
                var role = guild.getRoleById(roleId);
                if (role != null) guild.removeRoleFromMember(member, role).queue();
            });

            // sempre dá Fidalgo
            var fidalgo = guild.getRoleById(ROLE_FIDALGO);
            if (fidalgo != null) guild.addRoleToMember(member, fidalgo).queue();

            // pings selecionados
            if (session.isPingOracoes()) {
                var role = guild.getRoleById(ROLE_PING_ORACOES);
                if (role != null) guild.addRoleToMember(member, role).queue();
            }
            if (session.isPingBump()) {
                var role = guild.getRoleById(ROLE_PING_BUMP);
                if (role != null) guild.addRoleToMember(member, role).queue();
            }
            if (session.isPingReviver()) {
                var role = guild.getRoleById(ROLE_PING_REVIVER);
                if (role != null) guild.addRoleToMember(member, role).queue();
            }

            // ticket
            var ticket = jda.getTextChannelById(session.getTicketChannelId());
            if (ticket != null) {
                ticket.sendMessage("""
                    ✅ Verificação concluída! Bem-vindo(a) ao **Servo de Maria**, %s!
                    Que Nossa Senhora interceda por você. 🙏
                    
                    Você já pode acessar os canais do servidor.
                    """.formatted(member.getAsMention())).queue();
            }

            sessions.remove(session.getTicketChannelId());
            log.info("Candidato {} aceito por {}", userId, admId);
        });
    }

    public void recusarCandidato(String userId, String admId) {
        var session = buscarSessionPorUser(userId);
        if (session == null) return;

        var guild = jda.getGuilds().get(0);
        guild.retrieveMemberById(userId).queue(member -> {
            var ticket = jda.getTextChannelById(session.getTicketChannelId());
            if (ticket != null) {
                ticket.sendMessage("""
                        ❌ Infelizmente sua solicitação de entrada não foi aprovada.
                        Se acredita que houve um engano, entre em contato com a administração.
                        """).queue(msg ->
                        guild.kick(member)
                                .reason("Verificação recusada por " + admId)
                                .queueAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                );
            } else {
                guild.kick(member).reason("Verificação recusada por " + admId).queue();
            }

            sessions.remove(session.getTicketChannelId());
            log.info("Candidato {} recusado e kickado por {}", userId, admId);
        });
    }

    public void retomarVerificacao(String userId) {
        var session = buscarSessionPorUser(userId);
        if (session == null || session.getStep() != VerificationSession.Step.PAUSADO) return;

        session.setStep(VerificationSession.Step.AGUARDANDO_R2);

        var channel = jda.getTextChannelById(session.getTicketChannelId());
        if (channel != null) {
            channel.sendMessage("""
                    ▶️ Sua verificação foi retomada por um administrador.
                    Por favor, continue respondendo as perguntas.
                    """).queue();
            channel.sendMessage(PERGUNTA_2).queue();
        }
    }

    // ── Timeout / Lembretes ──────────────────────────────────────────────────

    @Scheduled(fixedRate = 60_000) // verifica a cada 1 min
    public void verificarTimeouts() {
        var agora = Instant.now();

        sessions.forEach((channelId, session) -> {
            if (session.getStep() == VerificationSession.Step.CONCLUIDO) return;
            if (session.getStep() == VerificationSession.Step.PAUSADO) return;

            var inativo = ChronoUnit.MILLIS.between(session.getLastActivity(), agora);

            if (inativo >= TIMEOUT_FECHAR_MS) {
                // 24h sem resposta → fecha
                fecharPorTimeout(session);
            } else if (inativo >= TIMEOUT_LEMBRETE_MS * (session.getLembretes() + 1)) {
                // manda lembrete a cada 30min
                enviarLembrete(session);
            }
        });
    }

    private void enviarLembrete(VerificationSession session) {
        var channel = jda.getTextChannelById(session.getTicketChannelId());
        if (channel == null) return;

        session.setLembretes(session.getLembretes() + 1);

        channel.sendMessage("""
                👋 <@%s>, você ainda está aí?
                Por favor, continue respondendo as perguntas de verificação.
                Caso não responda em %d horas, o ticket será fechado automaticamente.
                """.formatted(
                session.getUserId(),
                (TIMEOUT_FECHAR_MS - (TIMEOUT_LEMBRETE_MS * session.getLembretes())) / 3_600_000
        )).queue();
    }

    private void fecharPorTimeout(VerificationSession session) {
        var channel = jda.getTextChannelById(session.getTicketChannelId());
        if (channel != null) {
            channel.sendMessage("""
                    ⏰ Ticket encerrado por inatividade.
                    Se ainda deseja entrar no servidor, abra um novo ticket.
                    """).queue();
        }
        sessions.remove(session.getTicketChannelId());
        log.info("Ticket {} fechado por timeout", session.getTicketChannelId());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public VerificationSession getSession(String channelId) {
        return sessions.get(channelId);
    }

    private VerificationSession buscarSessionPorUser(String userId) {
        return sessions.values().stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}
