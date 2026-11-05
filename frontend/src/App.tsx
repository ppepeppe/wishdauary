import SharingGroupsPage from './pages/SharingGroupsPage';
import SharedDiaryFeedPage from './pages/SharedDiaryFeedPage';
import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/common/Layout';
import PublicOnlyRoute from './components/common/PublicOnlyRoute';
import RequireAppRole from './components/common/RequireAppRole';
import ChildSignupPage from './pages/ChildSignupPage';
import DiaryListPage from './pages/DiaryListPage';
import DiaryDetailPage from './pages/DiaryDetailPage';
import DiaryWritePage from './pages/DiaryWritePage';
import FamilyFeedPage from './pages/FamilyFeedPage';
import FamilyHomePage from './pages/FamilyHomePage';
import FamilyManagementPage from './pages/FamilyManagementPage';
import FamilySettingsPage from './pages/FamilySettingsPage';
import FamilySignupPage from './pages/FamilySignupPage';
import FeedPage from './pages/FeedPage';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import MyPage from './pages/MyPage';
import RootPage from './pages/RootPage';
import ShopGiftRequestPage from './pages/ShopGiftRequestPage';
import ShopNewProductPage from './pages/ShopNewProductPage';
import ShopProductDetailPage from './pages/ShopProductDetailPage';
import ShopWishlistPage from './pages/ShopWishlistPage';
import ShopCatalogPage from './pages/ShopCatalogPage';
import ShopSentRequestsPage from './pages/ShopSentRequestsPage';
import FamilyConnectionsPage from './pages/FamilyConnectionsPage';
import FamilyShopPage from './pages/FamilyShopPage';
import SignupRolePage from './pages/SignupRolePage';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<RootPage />} />

        <Route element={<PublicOnlyRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupRolePage />} />
          <Route path="/signup/child" element={<ChildSignupPage />} />
          <Route path="/signup/family" element={<FamilySignupPage />} />
        </Route>

        <Route element={<RequireAppRole allowedRole="child" />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/feed" element={<FeedPage />} />
          <Route path="/shop" element={<ShopCatalogPage />} />
          <Route path="/shop/catalog" element={<Navigate to="/shop" replace />} />
          <Route path="/shop/wishlist" element={<ShopWishlistPage />} />
          <Route path="/shop/requests" element={<ShopSentRequestsPage />} />
          <Route path="/shop/products/new" element={<ShopNewProductPage />} />
          <Route path="/shop/products/:productId" element={<ShopProductDetailPage />} />
          <Route path="/shop/products/:productId/request" element={<ShopGiftRequestPage />} />
          <Route path="/diaries/write" element={<DiaryWritePage />} />
          <Route path="/diaries" element={<DiaryListPage />} />
          <Route path="/sharing-groups" element={<SharingGroupsPage />} />
          <Route path="/family" element={<FamilyManagementPage />} />
        </Route>

        <Route element={<RequireAppRole allowedRole="family" />}>
          <Route path="/family-home" element={<FamilyHomePage />} />
          <Route path="/family-feed" element={<FamilyFeedPage />} />
          <Route path="/family-settings" element={<FamilySettingsPage />} />
          <Route path="/family-shop" element={<FamilyShopPage />} />
        </Route>

        <Route element={<RequireAppRole />}>
          <Route path="/family-connections" element={<FamilyConnectionsPage />} />
          <Route path="/shared-feed" element={<SharedDiaryFeedPage />} />
          <Route path="/diaries/:diaryId" element={<DiaryDetailPage />} />
          <Route path="/me" element={<MyPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
