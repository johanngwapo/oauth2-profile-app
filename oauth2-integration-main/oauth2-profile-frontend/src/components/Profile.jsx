import React, { useEffect, useState } from "react";
import axios from "axios";

export default function Profile() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/user", { withCredentials: true })
      .then((res) => setUser(res.data))
      .catch(() => (window.location.href = "/"));
  }, []);

  const handleLogout = () => {
    window.location.href = "http://localhost:8080/logout";
  };

  if (!user)
    return (
      <div className="flex items-center justify-center min-h-screen">
        <h3 className="text-xl font-semibold">Loading...</h3>
      </div>
    );

  return (
    <div className="min-h-screen flex justify-center items-center bg-gray-100">
      <div className="bg-white shadow-lg rounded-lg p-8 flex flex-col items-center space-y-4 w-80">
        <img
          src={user.avatarUrl}
          alt="Avatar"
          className="w-24 h-24 rounded-full shadow-md"
        />
        <h2 className="text-2xl font-bold text-gray-800">{user.displayName}</h2>
        <p className="text-gray-600">{user.email}</p>
        <button
          onClick={handleLogout}
          className="mt-4 w-full py-2 bg-red-500 text-white font-semibold rounded hover:bg-red-600 transition"
        >
          Logout
        </button>
      </div>
    </div>
  );
}
