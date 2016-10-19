export class GridModel<T> {
  // name: Array<any>;
  // status: string;
  // shape: string;
  // resources: Array<any>;
  //
  // constructor(name, status, shape, resources) {
  //   this.name = name;
  //   this.status = status;
  //   this.shape = shape;
  //   this.resources = resources;
  // }

  private _items: T[];

  constructor(items: T[]) {
    this._items = items;
  }

  get items(): T[] {
    return this._items;
  }
}
