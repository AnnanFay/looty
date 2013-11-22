//////////////////////////////////////////////////////////////
// Copyright (c) 2013 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 11/20/13 12:00 AM
//////////////////////////////////////////////////////////////

module Cgta.Views {
  export module SimpleView {
    var mod = angular.module("views.SimpleView", []);

    export class SimpleCtrl {
      stuff = "Nothing to see here"
      constructor($inventoryService: Cgta.Services.InventoryService) {
        console.log("Hello")
        $inventoryService.ensure().then(function() {
          console.log("Characters", $inventoryService.getCharacters())
        })
      }

    }

    mod.controller("SimpleCtrl",SimpleCtrl)

  }
}