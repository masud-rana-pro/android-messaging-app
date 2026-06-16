# Chat Detail Polish

Chat Detail now has the first pass of WhatsApp-like behavior while keeping ContactMe light mode as default.

## Added

- Message timestamps.
- Auto-scroll to the latest message.
- Scrollable message list.
- Rounded send action.
- Real and demo messages share the same bubble renderer.

## Notes

Default theme remains light. Dark mode exists in theme foundation, but UI verification should use light mode unless a dark-mode task is active.

## Verify

1. Open a real conversation.
2. Send multiple messages.
3. Confirm the list scrolls to the latest message.
4. Confirm each bubble shows a time.
5. Confirm dummy chat rows still show demo messages.
