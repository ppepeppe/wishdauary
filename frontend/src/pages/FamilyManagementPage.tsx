import { Navigate } from 'react-router-dom';

// Keep old family-management bookmarks pointing at the unified group manager.
function FamilyManagementPage() {
  return <Navigate to="/sharing-groups" replace />;
}

export default FamilyManagementPage;
