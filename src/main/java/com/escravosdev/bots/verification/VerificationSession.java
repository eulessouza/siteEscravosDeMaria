package com.escravosdev.bots.verification;

import lombok.Data;

import java.time.Instant;

@Data
public class VerificationSession {

    public enum Step {
        AGUARDANDO_R1,
        AGUARDANDO_R2,
        AGUARDANDO_R3,
        PAUSADO, // ia detectou problema - aguardando ADM
        CONCLUIDO
    }

    private final String userId;
    private final String ticketChannelId;
    private final String username;

    private Step step = Step.AGUARDANDO_R1;

    private String resposta1;
    private String resposta2;
    private String resposta3;

    private Instant lastActivity = Instant.now();
    private int lembretes = 0;

    private boolean pingOracoes  = false;
    private boolean pingBump     = false;
    private boolean pingReviver  = false;

    private String analiseR1;
    private String analiseR2;
    private String briefingFinal;

    public void updateActivity() {
        this.lastActivity = Instant.now();
        this.lembretes = 0;
    }

}
