package com.escravosdev.bots;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotReadyListener extends ListenerAdapter {

    private final RoleSyncService roleSyncService;

    @Override
    public void onReady(ReadyEvent event) {
        log.info("Bot online: {}", event.getJDA().getSelfUser().getName());
        roleSyncService.sync();
    }
}