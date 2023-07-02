const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.incrementAllNoticeCredits = functions.pubsub.schedule('every 1 hours').onRun((context) => {
  const database = admin.database();
  return database.ref('/Users').once('value')
      .then((usersSnapshot) => {
          const promises = [];
          usersSnapshot.forEach((userSnapshot) => {
              const uid = userSnapshot.key;
              const userModel = userSnapshot.val();
              const noticeCredit = userModel.noticeCredit;
              if (noticeCredit < 3) {
                  const newNoticeCredit = noticeCredit + 1;
                  const userRef = database.ref(`/Users/${uid}`);
                  promises.push(userRef.update({ noticeCredit: newNoticeCredit }));
              }
          });
          return Promise.all(promises);
      })
      .catch((error) => {
          console.error(error);
          return null;
      });
});