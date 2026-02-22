import React from 'react'
import AuthButton from './AuthButton'

const AlertBox = ({ title = 'Atenção', message, children, onClose }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 backdrop-blur-sm bg-black/30" onClick={onClose} />

            <div className="relative max-w-md w-full mx-4">
                <div className="bg-yellow-50 border border-yellow-300 text-yellow-900 text-sm font-medium px-4 py-4 rounded shadow-lg">
                    <div className="flex items-start justify-between">
                        <div>
                            <div className="font-semibold">{title}</div>
                            {message && <div className="mt-1 text-sm text-yellow-900">{message}</div>}
                            {children}
                        </div>
                        <div className="ml-4">
                            <button onClick={onClose} className="text-yellow-800 hover:opacity-80">✕</button>
                        </div>
                    </div>

                    <div className="mt-3">
                        <AuthButton />
                    </div>
                </div>
            </div>
        </div>
    )
}

export default AlertBox