import { useTranslation } from 'react-i18next';
import { HOST_URL } from './auth/auth.constants';
import { useFooterHook } from './useFooter.hook';

export default function Footer() {
  const { t } = useTranslation();
  const { accessToken, logoutHandler } = useFooterHook();
  return (
    <footer id="footer" style={{ display: accessToken ? 'flex' : 'block' }}>
      {accessToken && (
        <button type="button" className="logout" onClick={logoutHandler}>
          {t('footer.logout')}
        </button>
      )}
      <button
        type="button"
        onClick={() => {
          chrome.tabs.create({ url: `${HOST_URL}` + '/view' });
        }}>
        {/* TODO: view 페이지로 이동 시 (토큰 가지고 이동하는 방법 고안) */}
        {t('footer.homepage')}
      </button>
    </footer>
  );
}
