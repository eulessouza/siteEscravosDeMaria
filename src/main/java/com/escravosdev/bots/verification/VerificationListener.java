package com.escravosdev.bots.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.escravosdev.bots.verification.VerificationConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationListener extends ListenerAdapter {
    private final VerificationService verificationService;

    // ── Canal criado (Ticket Tool abre ticket) ───────────────────────────────

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannelType() != ChannelType.TEXT) return;

        var channel = event.getChannel().asTextChannel();
        var name    = channel.getName();

        // detecta ticket-XXXX
        if (!name.startsWith("ticket-")) return;

        log.info("Novo ticket detectado: {}", name);

        // aguarda um pouco pro Ticket Tool configurar o canal
        channel.getGuild().findMembersWithRoles(
                channel.getGuild().getRoleById(ROLE_MEEIRO)
        ).onSuccess(members -> {
            // busca o membro com acesso ao canal que seja Meeiro
            channel.getMembers().stream()
                    .filter(m -> !m.getUser().isBot())
                    .filter(m -> m.getRoles().stream()
                            .anyMatch(r -> r.getId().equals(ROLE_MEEIRO)))
                    .findFirst()
                    .ifPresent(member ->
                            verificationService.iniciarVerificacao(channel.getId(), member)
                    );
        });
    }

    // ── Mensagem recebida no ticket ──────────────────────────────────────────

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getChannelType() != ChannelType.TEXT) return;

        var channelName = event.getChannel().getName();
        if (!channelName.startsWith("ticket-")) return;

        var session = verificationService.getSession(event.getChannel().getId());
        if (session == null) return;

        verificationService.processarMensagem(
                event.getChannel().getId(),
                event.getAuthor().getId(),
                event.getMessage().getContentRaw()
        );
    }

    // ── Select menu (pings) ──────────────────────────────────────────────────

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("pings")) return;

        var selecionados = event.getValues();
        verificationService.processarSelecaoPings(
                event.getChannel().getId(),
                event.getUser().getId(),
                selecionados
        );

        var labels = selecionados.isEmpty() ? "Nenhum" : String.join(", ", selecionados);
        event.reply("Selecionado: **" + labels + "**\nClique em **Confirmar** quando estiver pronto.")
                .setEphemeral(true)
                .queue();
    }

    // ── Botões ───────────────────────────────────────────────────────────────

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        var id    = event.getComponentId();
        var admId = event.getUser().getId();

        // ── botões no ticket ──
        if (id.equals("pings_confirmar")) {
            verificationService.confirmarPings(event.getChannel().getId(), event.getUser().getId());
            event.reply("✅ Seleção confirmada!").setEphemeral(true).queue();
            return;
        }

        if (id.equals("pings_nenhum")) {
            verificationService.confirmarPings(event.getChannel().getId(), event.getUser().getId());
            event.reply("✅ Confirmado sem pings.").setEphemeral(true).queue();
            return;
        }

        // ── botões no canal ADM ──
        if (id.startsWith("aceitar_")) {
            var userId = id.replace("aceitar_", "");
            event.reply("✅ Candidato aceito por <@" + admId + ">").queue();
            verificationService.aceitarCandidato(userId, admId);
            return;
        }

        if (id.startsWith("recusar_")) {
            var userId = id.replace("recusar_", "");
            event.reply("❌ Candidato recusado por <@" + admId + ">").queue();
            verificationService.recusarCandidato(userId, admId);
            return;
        }

        if (id.startsWith("retomar_")) {
            var userId = id.replace("retomar_", "");
            event.reply("▶️ Verificação retomada por <@" + admId + ">").queue();
            verificationService.retomarVerificacao(userId);
        }
    }

}
