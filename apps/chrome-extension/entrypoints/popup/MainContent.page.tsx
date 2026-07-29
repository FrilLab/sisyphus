import { useTranslation } from 'react-i18next';
import { AuthFormField } from './auth/Auth.form';
import { useAuthStore } from './auth/auth.store';
import { SocialLoginButtons } from './auth/SocialLoginButtons.component';
import { useMessageStore } from './message.store';
import { NoteFormField } from './note/Note.form';

function messageTone(message: string): 'success' | 'error' | 'info' {
  if (message.includes('✅')) return 'success';
  if (message.includes('❌')) return 'error';
  return 'info';
}

export default function ContentPage() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const msg = useMessageStore((state) => state.msg);
  const { t } = useTranslation();

  return (
    <main id="main">
      <header className="page-header">
        <h1>{accessToken ? t('main.note.title') : t('main.auth.title')}</h1>
        <p>
          {accessToken ? t('main.note.description') : t('main.auth.description')}
        </p>
      </header>

      {msg && (
        <span className="state_msg" data-tone={messageTone(msg)} role="status">
          {msg}
        </span>
      )}

      {!accessToken ? (
        <>
          <AuthFormField />
          <hr />
          <SocialLoginButtons />
        </>
      ) : (
        <NoteFormField />
      )}
    </main>
  );
}
