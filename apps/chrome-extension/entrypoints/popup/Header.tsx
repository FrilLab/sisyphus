import { useTranslation } from 'react-i18next';
import { HOST_URL } from './auth/auth.constants';

export default function Header() {
  const { t } = useTranslation();
  return (
    <header id="header">
      <button
        className="logo_btn tooltip-wrapper"
        type="button"
        aria-label={t('header.tooltip')}
        onClick={() => {
          // TODO: url 변경
          chrome.tabs.create({ url: `${HOST_URL}` });
        }}>
        <img className="img_png" src="/png/diagram-logo.png" alt="logo"></img>
        <img className="img_text" src="/png/text-logo.png" alt="logo"></img>
        <span className="tooltip">{t('header.tooltip')}</span>
      </button>
    </header>
  );
}
