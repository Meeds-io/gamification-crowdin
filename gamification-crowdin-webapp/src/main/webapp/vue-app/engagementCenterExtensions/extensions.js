export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'crowdin',
    options: {
      rank: 90,
      image: '/gamification-crowdin/images/crowdin.png',
      match: (actionLabel) => [
        'stringCommentCreated',
        'suggestionAdded',
        'suggestionApproved',
        'approveSuggestion'
      ].includes(actionLabel),
      getLink: (realization) => {
        realization.link = realization.objectId;
        return realization.link;
      },
      isExtensible: true
    },
  });
}