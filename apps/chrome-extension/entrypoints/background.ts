import { defineBackground } from 'wxt/utils/define-background';
import { POPUP_HEIGHT, POPUP_WIDTH } from './popup/popup.constants';

export default defineBackground(() => {
  const setSelectedWord = (selectedWord: string) =>
    new Promise<void>((resolve, reject) => {
      chrome.storage.local.set({ selectedWord }, () => {
        const lastErr = chrome.runtime.lastError;
        if (lastErr) reject(lastErr);
        else resolve();
      });
    });

  const openSelectionPopup = async (selectedWord: string) => {
    const trimmed = selectedWord.trim();
    if (!trimmed) return { ok: false, reason: 'empty-selection' };

    const popupUrl = chrome.runtime.getURL(
      `popup.html?selectedWord=${encodeURIComponent(trimmed)}`,
    );

    try {
      await setSelectedWord(trimmed);
    } catch (err) {
      return {
        ok: false,
        reason: 'storage-set-failed',
        error: (err as Error)?.message,
      };
    }

    try {
      await chrome.action.openPopup();
      return { ok: true, opened: 'action-popup' };
    } catch (err) {
      try {
        await new Promise<void>((resolve, reject) => {
          chrome.windows.create(
            {
              url: popupUrl,
              type: 'popup',
              width: POPUP_WIDTH,
              height: POPUP_HEIGHT,
              focused: true,
            },
            () => {
              const winErr = chrome.runtime.lastError;
              if (winErr) reject(winErr);
              else resolve();
            },
          );
        });
        return {
          ok: true,
          opened: 'window-popup',
          fallbackFrom: (err as Error)?.message,
        };
      } catch (winErr) {
        return {
          ok: false,
          reason: 'window-create-failed',
          error: (winErr as Error)?.message,
          openPopupError: (err as Error)?.message,
        };
      }
    }
  };

  chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
      id: 'showSelectedWord',
      title:
        chrome.i18n.getMessage('context_add_to_vocab') || 'Add to vocabulary',
      contexts: ['selection'],
    });
  });

  chrome.runtime.onMessage.addListener(
    (
      message: any, // (type) any - 메시지 페이로드
      _sender: chrome.runtime.MessageSender, // (type) MessageSender - 보낸 주체
      sendResponse: (response?: any) => void, // (type) function - 응답 콜백
    ) => {
      if (message?.type !== 'openPopupWithSelection') return;

      const selectedWord = String(message?.selectedWord ?? '');

      openSelectionPopup(selectedWord).then(sendResponse);
      return true;
    },
  );

  chrome.commands.onCommand.addListener(async (command) => {
    if (command !== 'open-selection') return;

    const [tab] = await chrome.tabs.query({
      active: true,
      currentWindow: true,
    });
    if (!tab?.id) return;

    try {
      const results = await chrome.scripting.executeScript({
        target: { tabId: tab.id, allFrames: true }, // (type) boolean - 모든 프레임에 실행
        func: () => window.getSelection()?.toString().trim() ?? '',
      });

      // (role) results에서 비어있지 않은 첫 값을 선택, type: string
      const selectedWord =
        results
          .map((r) => String(r.result ?? '').trim())
          .find((v) => v.length > 0) ?? '';

      await openSelectionPopup(selectedWord);
    } catch (err) {
      console.warn('선택 텍스트를 가져오지 못했습니다.', err);
    }
  });

  chrome.contextMenus.onClicked.addListener(async (info, tab) => {
    if (!info.selectionText) return;

    const selectedWord = info.selectionText;

    if (info.menuItemId === 'showSelectedWord') {
      openSelectionPopup(selectedWord);
    }
  });
});
