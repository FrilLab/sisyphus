import { useEffect } from 'react';
import './styles/app.css';
import Footer from './Footer';
import Header from './Header';
import ContentPage from './MainContent.page';
import { clearLegacyAuthStorage } from './auth/auth.store';

function App() {
  useEffect(() => {
    clearLegacyAuthStorage();
  }, []);

  // TODO: translation 작성
  // TODO: wxt.config.ts 도 translation 가능한지 확인
  // TODO: apple oauth 추가
  return (
    <div className="app-shell">
      <Header />
      <ContentPage />
      <Footer />
    </div>
  );
}
export default App;
