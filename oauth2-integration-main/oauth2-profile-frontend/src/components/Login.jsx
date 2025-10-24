import React, { useState } from "react";

export default function Login() {
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (provider) => {
    setIsLoading(true);
    try {
      // Use Vite's import.meta.env with VITE_ prefix
      const baseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      window.location.href = `${baseUrl}/oauth2/authorization/${provider}`;
    } catch (error) {
      console.error("Failed to initiate login:", error);
      alert("Failed to connect to the authentication server. Please try again.");
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center bg-gray-100">
      <div className="bg-white shadow-lg rounded-lg p-10 flex flex-col items-center space-y-6">
        <h2 className="text-3xl font-bold text-gray-800">OAuth2 Login</h2>
        <button
          onClick={() => handleLogin("google")}
          disabled={isLoading}
          className={`w-64 py-2 px-4 bg-red-500 text-white font-semibold rounded hover:bg-red-600 transition ${
            isLoading ? "opacity-50 cursor-not-allowed" : ""
          }`}
        >
          {isLoading ? "Redirecting..." : "Login with Google"}
        </button>
        <button
          onClick={() => handleLogin("github")}
          disabled={isLoading}
          className={`w-64 py-2 px-4 bg-gray-800 text-white font-semibold rounded hover:bg-gray-900 transition ${
            isLoading ? "opacity-50 cursor-not-allowed" : ""
          }`}
        >
          {isLoading ? "Redirecting..." : "Login with GitHub"}
        </button>
      </div>
    </div>
  );
}