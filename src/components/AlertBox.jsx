import React from 'react';

const AlertBox = ({ message }) => {
    return (
        <div className="bg-black-100 border border-black-400 text-white text-sm font-bold mb-700 px-4 py-3 rounded relative" role="alert">
            <span className="block sm:inline">{message}</span>
        </div>
    );
};

export default AlertBox;