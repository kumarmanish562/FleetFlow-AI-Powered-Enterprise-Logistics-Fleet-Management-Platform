import { useMutation } from '@tanstack/react-query';
import { api } from '../api/client.js';

export function useOperationalAI() {
  return useMutation({
    mutationFn: async ({ question, conversationId }) => {
      const { data } = await api.post('/ai/operational/query', { question, conversationId }, { timeout: 90_000 });
      return data;
    },
  });
}
