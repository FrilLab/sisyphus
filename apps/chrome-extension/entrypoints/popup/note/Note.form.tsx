import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNoteHook } from './useNote.hook';

export const NoteFormField = () => {
  const { register, handleSubmit, errors, isSubmitting, setValue } =
    useNoteHook();
  const { t } = useTranslation();

  useEffect(() => {
    chrome.storage.local.get(['selectedWord'], (res) => {
      const selectedWord = String(res?.selectedWord ?? '').trim();
      if (!selectedWord) return;

      // (param) field: 'title' - 주입할 필드명
      // (param) value: string - 주입할 값
      // (param) options: object - RHF가 변경 인식하도록 옵션
      setValue('title', selectedWord, {
        shouldDirty: true,
        shouldTouch: true,
        shouldValidate: true,
      });

      // 원하면 주입 후 삭제해서 다음 열 때 남아있지 않게:
      // chrome.storage.local.remove(['selectedWord']);
    });
  }, [setValue]);

  return (
    <div>
      <form className="form_tag" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="title">{t('main.note.tit')}</label>
          <input
            id="title"
            type="text"
            placeholder={t('main.note.tit_place')}
            {...register('title', { required: t('main.note.tit_required') })}
          />
          {errors.title && <p className="error">{errors.title.message}</p>}
        </div>

        <div className="field">
          <label htmlFor="subTitle">{t('main.note.sub')}</label>
          <input
            id="subTitle"
            type="text"
            placeholder={t('main.note.sub_place')}
            {...register('subTitle')}
          />
        </div>

        <button type="submit" className="save-button" disabled={isSubmitting}>
          {t(isSubmitting ? 'main.note.ing_save' : 'main.note.save')}
        </button>
      </form>
    </div>
  );
};
