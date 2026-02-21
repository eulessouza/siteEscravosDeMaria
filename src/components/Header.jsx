import React from 'react'
import Button from './Button'

export default function Header() {
  return (
    <header className="bg-blue-700 text-white">
      <div className="max-w-4xl mx-auto flex items-center justify-between p-4">
        <div>
          <h1 className="text-2xl font-bold">Escravos de Maria</h1>
          <h2 className="text-sm text-blue-100">Um Blog feito por Católicos que amam a Igreja</h2>
        </div>
        <nav className="flex-row sm:flex-row items-start space-x-2">
          <Button href="/" variant="ghost">Início</Button>
          <Button href="/rosario" variant="ghost">Rosário</Button>
          <Button href="/publicacoes" variant="ghost">Publicações</Button>
          <Button href="/forum" variant="ghost">Fórum</Button>
        </nav>
      </div>
    </header>
  )
}
