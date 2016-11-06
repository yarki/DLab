import  { ComputationalResourceApplication } from "./computationalResourceApplication.model";

export class ComputationalResourceApplicationTemplate {
  version: string;
  applications: Array<ComputationalResourceApplication>

  fromJSON() : void { }
}
