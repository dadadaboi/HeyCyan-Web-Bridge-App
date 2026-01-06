import { registerPlugin } from "@capacitor/core";

import type { HeyCyanGlassesPlugin } from "./definitions";

const HeyCyanGlasses = registerPlugin<HeyCyanGlassesPlugin>("HeyCyanGlasses", {
  web: () => import("./web").then((m) => new m.HeyCyanGlassesWeb()),
});

export * from "./definitions";
export { HeyCyanGlasses };
