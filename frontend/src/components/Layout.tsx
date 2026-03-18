import { Link, useAuth } from 'react-router-dom'

interface LayoutProps {
  children: React.ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const { user, logout } = useAuth()
  return (
    <div className="layout">
      <header>
        <nav>
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/picks">Picks</Link>
          <Link to="/live">Live</Link>
          {user?.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
          <span>{user?.displayName}</span>
          <button onClick={logout}>Logout</button>
        </nav>
      </header>
      <main>{children}</main>
    </div>
  )
}
