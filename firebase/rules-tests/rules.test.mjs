// Firestore security rules tests. Run: npm test (starts the local Firestore emulator).
import { test, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, getDocs, updateDoc, deleteDoc, collection, serverTimestamp, writeBatch, Timestamp } from 'firebase/firestore';

let env;
const CUSTOMER = 'cust1', HELPER = 'help1', OTHER = 'other1';

const task = (over = {}) => ({
  ownerId: CUSTOMER, ownerName: 'Ramesh', status: 'submitted', categoryId: 'repairs',
  locationQuery: 'Civil Lines, Kanpur', title: 'Kitchen tap is leaking', details: 'Need a plumber today',
  minBudget: 300, maxBudget: 800, createdAt: serverTimestamp(), chosenHelperId: null, chosenAmount: null, ...over,
});
const offer = (over = {}) => ({
  helperId: HELPER, helperName: 'Suresh', amount: 650, message: 'Can come at 6 pm', createdAt: serverTimestamp(), status: 'sent', ...over,
});
const db = (uid) => (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).firestore();

/** Seed data ignoring the rules. */
async function seed(fn) { await env.withSecurityRulesDisabled(async (ctx) => fn(ctx.firestore())); }
const seededTask = (over = {}) => ({ ...task(over), createdAt: Timestamp.now() });
const seededOffer = (over = {}) => ({ ...offer(over), createdAt: Timestamp.now() });

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-fixorax',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8'), host: '127.0.0.1', port: 8080 },
  });
});
after(async () => { await env.cleanup(); });
beforeEach(async () => { await env.clearFirestore(); });

// ---------- tasks ----------
test('guests (not logged in) cannot read or post tasks', async () => {
  await seed((f) => setDoc(doc(f, 'tasks/t1'), seededTask()));
  await assertFails(getDoc(doc(db(null), 'tasks/t1')));
  await assertFails(setDoc(doc(db(null), 'tasks/t2'), task()));
});

test('a logged-in customer can post their own task', async () => {
  await assertSucceeds(setDoc(doc(db(CUSTOMER), 'tasks/t1'), task()));
});

test('nobody can post a task in someone else\'s name', async () => {
  await assertFails(setDoc(doc(db(OTHER), 'tasks/t1'), task()));
});

test('bad task data is refused (empty title, negative budget, extra fields, wrong status, fake time)', async () => {
  const c = db(CUSTOMER);
  await assertFails(setDoc(doc(c, 'tasks/a'), task({ title: '' })));
  await assertFails(setDoc(doc(c, 'tasks/b'), task({ maxBudget: -5 })));
  await assertFails(setDoc(doc(c, 'tasks/c'), task({ isAdmin: true })));
  await assertFails(setDoc(doc(c, 'tasks/d'), task({ status: 'accepted' })));
  await assertFails(setDoc(doc(c, 'tasks/e'), task({ createdAt: Timestamp.fromMillis(0) })));
  await assertFails(setDoc(doc(c, 'tasks/f'), task({ chosenHelperId: HELPER })));
});

test('helpers can see open tasks; only the owner can change or delete them', async () => {
  await seed((f) => setDoc(doc(f, 'tasks/t1'), seededTask()));
  await assertSucceeds(getDoc(doc(db(HELPER), 'tasks/t1')));
  await assertFails(updateDoc(doc(db(HELPER), 'tasks/t1'), { status: 'cancelled' }));
  await assertFails(deleteDoc(doc(db(HELPER), 'tasks/t1')));
  await assertSucceeds(updateDoc(doc(db(CUSTOMER), 'tasks/t1'), { status: 'cancelled' }));
  await assertFails(updateDoc(doc(db(CUSTOMER), 'tasks/t1'), { ownerId: OTHER }));
  await assertSucceeds(deleteDoc(doc(db(CUSTOMER), 'tasks/t1')));
});

// ---------- offers ----------
test('a helper can send one offer in their own name, only on an open task', async () => {
  await seed(async (f) => {
    await setDoc(doc(f, 'tasks/open'), seededTask());
    await setDoc(doc(f, 'tasks/closed'), seededTask({ status: 'accepted', chosenHelperId: OTHER }));
  });
  const h = db(HELPER);
  await assertFails(setDoc(doc(h, `tasks/open/offers/${OTHER}`), offer({ helperId: OTHER })));
  await assertFails(setDoc(doc(h, `tasks/open/offers/${HELPER}`), offer({ amount: 0 })));
  await assertFails(setDoc(doc(h, `tasks/open/offers/${HELPER}`), offer({ amount: 99.5 })));
  await assertFails(setDoc(doc(h, `tasks/closed/offers/${HELPER}`), offer()));
  await assertSucceeds(setDoc(doc(h, `tasks/open/offers/${HELPER}`), offer()));
});

test('a customer cannot send an offer on their own task', async () => {
  await seed((f) => setDoc(doc(f, 'tasks/t1'), seededTask()));
  await assertFails(setDoc(doc(db(CUSTOMER), `tasks/t1/offers/${CUSTOMER}`), offer({ helperId: CUSTOMER })));
});

test('offers are private: only that helper and the task owner can read them', async () => {
  await seed(async (f) => {
    await setDoc(doc(f, 'tasks/t1'), seededTask());
    await setDoc(doc(f, `tasks/t1/offers/${HELPER}`), seededOffer());
  });
  await assertSucceeds(getDoc(doc(db(HELPER), `tasks/t1/offers/${HELPER}`)));
  await assertSucceeds(getDocs(collection(db(CUSTOMER), 'tasks/t1/offers')));
  await assertFails(getDoc(doc(db(OTHER), `tasks/t1/offers/${HELPER}`)));
  await assertFails(getDocs(collection(db(OTHER), 'tasks/t1/offers')));
  await assertFails(getDocs(collection(db(HELPER), 'tasks/t1/offers'))); // helpers can't see rivals' prices
});

test('only the task owner can choose an offer, and only its status can change', async () => {
  await seed(async (f) => {
    await setDoc(doc(f, 'tasks/t1'), seededTask());
    await setDoc(doc(f, `tasks/t1/offers/${HELPER}`), seededOffer());
  });
  await assertFails(updateDoc(doc(db(HELPER), `tasks/t1/offers/${HELPER}`), { status: 'accepted' }));
  await assertFails(updateDoc(doc(db(CUSTOMER), `tasks/t1/offers/${HELPER}`), { amount: 1 }));
  const c = db(CUSTOMER);
  const b = writeBatch(c);
  b.update(doc(c, `tasks/t1/offers/${HELPER}`), { status: 'accepted' });
  b.update(doc(c, 'tasks/t1'), { status: 'accepted', chosenHelperId: HELPER, chosenAmount: 650 });
  await assertSucceeds(b.commit());
});

// ---------- chat ----------
test('chat is only between the owner and the chosen helper', async () => {
  await seed(async (f) => {
    await setDoc(doc(f, 'tasks/t1'), seededTask({ status: 'accepted', chosenHelperId: HELPER, chosenAmount: 650 }));
  });
  const msg = (uid, role) => ({ senderUid: uid, senderRole: role, text: 'Namaste', createdAt: serverTimestamp() });
  await assertSucceeds(setDoc(doc(db(CUSTOMER), 'tasks/t1/messages/m1'), msg(CUSTOMER, 'customer')));
  await assertSucceeds(setDoc(doc(db(HELPER), 'tasks/t1/messages/m2'), msg(HELPER, 'worker')));
  await assertSucceeds(getDocs(collection(db(HELPER), 'tasks/t1/messages')));
  await assertFails(setDoc(doc(db(OTHER), 'tasks/t1/messages/m3'), msg(OTHER, 'worker')));
  await assertFails(getDocs(collection(db(OTHER), 'tasks/t1/messages')));
  await assertFails(setDoc(doc(db(HELPER), 'tasks/t1/messages/m4'), msg(CUSTOMER, 'customer'))); // can't fake sender
  await assertFails(setDoc(doc(db(HELPER), 'tasks/t1/messages/m5'), { ...msg(HELPER, 'worker'), text: 'x'.repeat(1001) }));
});

test('before a helper is chosen, no helper can chat', async () => {
  await seed((f) => setDoc(doc(f, 'tasks/t1'), seededTask()));
  await assertFails(setDoc(doc(db(HELPER), 'tasks/t1/messages/m1'),
    { senderUid: HELPER, senderRole: 'worker', text: 'hi', createdAt: serverTimestamp() }));
});

// ---------- helper's own list ----------
test('users/{uid}/offers is private to that user', async () => {
  await assertSucceeds(setDoc(doc(db(HELPER), `users/${HELPER}/offers/t1`), { taskId: 't1' }));
  await assertSucceeds(getDocs(collection(db(HELPER), `users/${HELPER}/offers`)));
  await assertFails(getDocs(collection(db(OTHER), `users/${HELPER}/offers`)));
  await assertFails(setDoc(doc(db(OTHER), `users/${HELPER}/offers/t2`), { taskId: 't2' }));
});

// ---------- account deletion ----------
test('a user can delete everything they created (account deletion)', async () => {
  await seed(async (f) => {
    await setDoc(doc(f, 'tasks/t1'), seededTask({ status: 'accepted', chosenHelperId: HELPER }));
    await setDoc(doc(f, `tasks/t1/offers/${HELPER}`), seededOffer({ status: 'accepted' }));
    await setDoc(doc(f, 'tasks/t1/messages/m1'), { senderUid: HELPER, senderRole: 'worker', text: 'hi', createdAt: Timestamp.now() });
    await setDoc(doc(f, `users/${HELPER}/offers/t1`), { taskId: 't1' });
  });
  // helper removes their offer, own messages and list
  await assertSucceeds(deleteDoc(doc(db(HELPER), `tasks/t1/offers/${HELPER}`)));
  await assertSucceeds(deleteDoc(doc(db(HELPER), 'tasks/t1/messages/m1')));
  await assertSucceeds(deleteDoc(doc(db(HELPER), `users/${HELPER}/offers/t1`)));
  // customer removes the task
  await assertSucceeds(deleteDoc(doc(db(CUSTOMER), 'tasks/t1')));
});
