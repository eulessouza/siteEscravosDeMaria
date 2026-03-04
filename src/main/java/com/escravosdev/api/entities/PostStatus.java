package com.escravosdev.api.entities;

public enum PostStatus {
    DRAFT,             // rascunho — só blog usa
    PENDING_APPROVAL,  // aguardando aprovação — fórum e dúvidas
    PUBLISHED,         // publicado
    CLOSED,            // dúvida respondida e fechada pelo orientador ou adm
    ARCHIVED,          // arquivado pelo adm
    REJECTED           // rejeitado pelo adm
}
