import { Navigate, Outlet } from 'react-router-dom';
import { getAuthSession, getDefaultRouteForRole } from '../../services/session/authSession';

function PublicOnlyRoute() {
  const authSession = getAuthSession();

  if (!authSession) {
    return <Outlet />;
  }

  return <Navigate to={getDefaultRouteForRole(authSession.appRole)} replace />;
}

export default PublicOnlyRoute;
