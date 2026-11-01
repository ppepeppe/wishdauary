import { Navigate, Outlet } from 'react-router-dom';
import type { AppRole } from '../../types/auth';
import { getAuthSession, getDefaultRouteForRole } from '../../services/session/authSession';

interface RequireAppRoleProps {
  allowedRole?: AppRole;
}

function RequireAppRole({ allowedRole }: RequireAppRoleProps) {
  const authSession = getAuthSession();

  if (!authSession) {
    return <Navigate to="/" replace />;
  }

  if (allowedRole && authSession.appRole !== allowedRole) {
    return <Navigate to={getDefaultRouteForRole(authSession.appRole)} replace />;
  }

  return <Outlet />;
}

export default RequireAppRole;
