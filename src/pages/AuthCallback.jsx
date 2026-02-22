import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function AuthCallback() {
  const { handleDiscordCallback } = useAuth()
  const [status, setStatus] = useState('Processando autenticação...')
  const [error, setError] = useState(null)

  useEffect(() => {
    const processCallback = async () => {
      try {
        // Pega o code dos parâmetros de URL
        const params = new URLSearchParams(window.location.search)
        const code = params.get('code')

        if (!code) {
          throw new Error('Código de autorização não encontrado')
        }

        setStatus('Trocando código por token...')
        const success = await handleDiscordCallback(code)

        if (success) {
          setStatus('Autenticação bem-sucedida! Redirecionando...')
          // Redireciona para home após sucesso
          setTimeout(() => {
            window.location.href = '/'
          }, 1500)
        } else {
          throw new Error('Falha na autenticação')
        }
      } catch (err) {
        console.error('Erro no callback:', err)
        setError(err.message)
        setStatus('Erro ao autenticar')
      }
    }

    processCallback()
  }, [handleDiscordCallback])

  return (
    <div className="min-h-screen bg-white dark:bg-gray-900 flex items-center justify-center">
      <div className="text-center">
        <div className="mb-4">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-brand-light dark:border-brand-dark"></div>
        </div>
        <p className="text-lg font-medium text-brand-dark dark:text-white">{status}</p>
        {error && (
          <div className="mt-4 text-red-500">
            <p className="font-semibold">Erro: {error}</p>
            <a href="/" className="text-brand-light hover:underline mt-2 inline-block">
              Voltar para home
            </a>
          </div>
        )}
      </div>
    </div>
  )
}
