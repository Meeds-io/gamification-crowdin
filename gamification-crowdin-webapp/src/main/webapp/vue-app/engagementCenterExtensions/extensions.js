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
        try {
          const objId = JSON.parse(realization.objectId);
          realization.link = objId.stringUrl;
          return realization.link;
        } catch (e) {
          return null;
        }
      },
      isExtensible: true
    },
  });
}