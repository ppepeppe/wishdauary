import { Navigate } from 'react-router-dom';
import LandingPage from './LandingPage';
import { getAuthSession, getDefaultRouteForRole } from '../services/session/authSession';

function RootPage() {
  const authSession = getAuthSession();

  if (!authSession) {
    return <LandingPage />;
  }

  return <Navigate to={getDefaultRouteForRole(authSession.appRole)} replace />;
}

export default RootPage;
