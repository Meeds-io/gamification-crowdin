export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'crowdin',
    options: {
      rank: 80,
      image: '/gamification-crowdin/images/crowdin.png',
      match: (actionLabel) => [
        'stringComment.created',
        'suggestion.added',
        'suggestion.approved',
        'approve.suggestion'
      ].includes(actionLabel),
      getLink: (realization) => {
        realization.link = realization.objectId;
        return realization.link;
      },
      isExtensible: true
    },
  });
}