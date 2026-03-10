package com.escravosdev.bots.verification;

public class VerificationConstants {
    private VerificationConstants() {}

    // ── Cargos ──────────────────────────────────────────────────────────────
    public static final String ROLE_PLEBEU          = "1456879744053149827";
    public static final String ROLE_MEEIRO          = "1456879974219907206";
    public static final String ROLE_FIDALGO         = "1456876613303271464";

    // Religião
    public static final String ROLE_CATOLICO        = "1457031371804381247";
    public static final String ROLE_ORTODOXO        = "1457031659319988296";
    public static final String ROLE_PROTESTANTE     = "1457031784167374972";
    public static final String ROLE_PAGAO           = "1457031879365759323";
    public static final String ROLE_MUCULMANO       = "1457032302222639149";
    public static final String ROLE_JUDEU           = "1457032315355267174";
    public static final String ROLE_ATEU            = "1457032320035848233";
    public static final String ROLE_SEM_DENOM       = "1463638998235742351";
    public static final String ROLE_RECEM_CONVERTIDO = "1457033311418454136";

    // Gênero
    public static final String ROLE_HOMEM           = "1457044028003455121";
    public static final String ROLE_MULHER          = "1457044165472026848";

    // Pings
    public static final String ROLE_PING_ORACOES    = "1457082174628172078";
    public static final String ROLE_PING_REVIVER    = "1457425887917117611";
    public static final String ROLE_PING_BUMP       = "1457425817742217458";

    // ── Canais ──────────────────────────────────────────────────────────────
    public static final String CANAL_SEGUNDA_ETAPA  = "1456892158744465479";
    public static final String CANAL_ADM            = "1481015733159723110";

    // ── Tempos ──────────────────────────────────────────────────────────────
    public static final long TIMEOUT_LEMBRETE_MS    = 60 * 60 * 1000L;  // 1h
    public static final long TIMEOUT_FECHAR_MS      = 24 * 60 * 60 * 1000L; // 24h

    // ── Perguntas ────────────────────────────────────────────────────────────
    public static final String PERGUNTA_1 = """
            ## Verificação
            Seja bem-vindo(a) %s,
            Você está solicitando entrada em um servidor católico fiel ao Magistério da Igreja e à autoridade legítima do Papa.
            Este não é um espaço neutro. É uma comunidade confessional. A permanência aqui exige postura compatível.
            Responda com clareza e objetividade:

            **1. Qual é a sua religião?**
            Se for católico(a), você é recém-convertido(a), está em processo de conversão ou é católico(a) desde sempre?
            Você reconhece a autoridade do Papa e do Magistério atual da Igreja?

            **2. Caso discorde de algum ensinamento da Igreja, como costuma agir?**
            Explique brevemente sua postura.

            **3. Você já leu o Catecismo da Igreja Católica, integralmente ou em partes?**
            Já estudou outros documentos oficiais (Concílios, Encíclicas, Compêndio, etc.) ou livros de formação católica?
            Se sim, quais?

            **4. Qual é o seu sexo?**

            **5. Por que deseja participar dessa comunidade?**
            Seja específico quanto às suas intenções.

            **6. Onde encontrou o servidor?**
            Foi indicação de alguém? Se sim, informe o nome.
            Se foi na internet, especifique onde.
            """;

    public static final String PERGUNTA_2 = """
            Antes de ser admitido, informamos:
            - **Este servidor não permite:**
              * Pornografia ou qualquer conteúdo imoral
              * Ataques à Igreja, ao Papa ou ao Magistério
              * Sedevacantismo
              * Militância político-partidária
              * Teorias conspiratórias
              * Desordem deliberada ou provocações

            - Você concorda integralmente em respeitar essas normas?
            """;

    public static final String PERGUNTA_3 = """
            - **Você deseja receber algum dos seguintes cargos?**
              * 🙏 **Avisos de Oração** — Notifica os terços e eventos de orações.
              * 🔔 **Lembrete de Bump** — Lembrar de dar o comando pro bot pra ajudar o server.
              * 💬 **Incentivo ao Chat** — Lembrar de mandar mensagem no chat para animar.

            Nosso servidor tem uma **tag**: **JHS** (*Jesus Hominum Salvator*).
            Use nossa tag para participar dos nossos sorteios!

            Selecione os cargos que deseja abaixo (ou nenhum):
            """;
}
