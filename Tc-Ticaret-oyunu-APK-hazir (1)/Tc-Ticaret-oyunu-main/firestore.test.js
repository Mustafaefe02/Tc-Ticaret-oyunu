const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

test("unauthenticated read is denied", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("holdings").get());
});

test("authenticated user can create their own holding document", async () => {
  const aliceDb = testEnv.authenticatedContext("alice_123").firestore();
  const payload = {
    userId: "alice_123",
    holdingName: "Koçer Holding",
    ownerName: "Alice",
    netWorth: 50000,
    cash: 50000,
    companyCount: 1,
    totalEmployees: 2,
    prestige: 10,
    createdAt: new Date(),
    updatedAt: new Date()
  };
  await assertSucceeds(aliceDb.collection("holdings").doc("alice_123").set(payload));
});

test("user cannot overwrite or create holding document of another user", async () => {
  const bobDb = testEnv.authenticatedContext("bob_456").firestore();
  const payload = {
    userId: "alice_123",
    holdingName: "Hacked Holding",
    ownerName: "Bob",
    netWorth: 100000,
    cash: 100000,
    companyCount: 1,
    totalEmployees: 2,
    prestige: 10,
    createdAt: new Date(),
    updatedAt: new Date()
  };
  await assertFails(bobDb.collection("holdings").doc("alice_123").set(payload));
});

test("authenticated user can read other users holding for leaderboard", async () => {
  const aliceDb = testEnv.authenticatedContext("alice_123").firestore();
  await aliceDb.collection("holdings").doc("alice_123").set({
    userId: "alice_123",
    holdingName: "Koçer Holding",
    ownerName: "Alice",
    netWorth: 50000,
    cash: 50000,
    companyCount: 1,
    totalEmployees: 2,
    prestige: 10,
    createdAt: new Date(),
    updatedAt: new Date()
  });

  const bobDb = testEnv.authenticatedContext("bob_456").firestore();
  await assertSucceeds(bobDb.collection("holdings").doc("alice_123").get());
  await assertSucceeds(bobDb.collection("holdings").get());
});
