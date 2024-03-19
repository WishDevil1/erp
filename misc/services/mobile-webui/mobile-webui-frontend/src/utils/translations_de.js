const translations = {
  appName: 'metasfresh mobile',
  error: {
    PleaseTryAgain: 'Oops, das sollte nicht passieren',
    network: {
      noResponse: 'Verbindung Fehler',
    },
    qrCode: {
      invalid: 'Ungültiger QR Code',
    },
  },
  general: {
    Yes: 'Ja',
    No: 'Nein',
    OK: 'OK',
    DocumentNo: 'Dokument Nr',
    Product: 'Produkt',
    ProductValue: 'Artikelnummer',
    Locator: 'Lagerort',
    QRCode: 'QR-Code',
    QtyToPick: 'Pick Menge',
    QtyToPick_Total: 'Pick Menge (total)',
    QtyPicked: 'Menge gepickt',
    QtyMoved: 'Menge bewegt',
    QtyToMove: 'Bewegungsmenge',
    QtyRejected: 'verworfen',
    CatchWeight: 'Gewicht',
    BestBeforeDate: 'MHD',
    LotNo: 'Lot-Nr',
    DropToLocator: 'Ziel Lagerort',
    cancelText: 'Abbrechen',
    closeText: 'Schließen',
    clearText: 'Lösen',
    reOpenText: 'Wieder öffnen',
    scanQRCode: 'QR scannen',
    Back: 'Zurück',
    Home: 'Home',
    filter: {
      showResults: 'Ergebnisse anzeigen (%(count)s)',
      clearFilters: 'Filter löschen',
    },
    workplace: 'Arbeitsplatz',
    workstation: 'Arbeitsstation',
  },
  login: {
    submitButton: 'Login',
    alternativeMethods: 'Wechseln zu...',
    authMethod: {
      qrCode: 'QR Code',
      userAndPass: 'Passwort',
    },
  },
  logout: 'Abmelden',
  mobileui: {
    manufacturing: {
      appName: 'Produktion',
    },
    picking: {
      appName: 'Kommissionierung',
    },
    distribution: {
      appName: 'Bereitstellung',
    },
  },
  components: {
    BarcodeScannerComponent: {
      scanTextPlaceholder: 'scan...',
      scanWorkplacePlaceholder: 'Arbeitsplatz scannen...',
      scanWorkstationPlaceholder: 'Arbeitsstation scannen...',
    },
  },
  activities: {
    scanBarcode: {
      defaultCaption: 'Scan',
      invalidScannedBarcode: 'Code ist ungültig',
    },
    picking: {
      PickingLine: 'Pick Zeile',
      PickHU: 'HU kommissionieren',
      scanQRCode: 'QR scannen',
      notEligibleHUBarcode: 'HU Code passt nicht',
      qtyAboveMax: '%(qtyDiff)s über max', // TODO verify trl
      notPositiveQtyNotAllowed: 'Null oder negative Menge nicht erlaubt', // TODO verify trl
      confirmDone: 'OK',
      rejectedPrompt: 'Es gibt %(qtyRejected)s %(uom)s ungepickte Mengen. Warum?',
      unPickBtn: 'Rückgängig',
      target: 'Soll',
      picked: 'Ist',
      switchToManualInput: 'Manuell',
      switchToQrCodeInput: 'QR scannen',
      skip: 'Überspringen',
      scanTargetHU: 'Ziel HU scannen',
      qrcode: {
        missingQty: 'Der gescannte QR-Code enthält keine Mengenangaben!',
        differentUOM: 'Der gescannte QR UOM stimmt nicht mit dem Ziel überein!',
      },
    },
    distribution: {
      DistributionLine: 'Bereitstellung Zeile',
      target: 'Soll',
      picked: 'Ist',
      scanHU: 'Scan HU',
      scanLocator: 'Scan Ziel Lagerort',
      invalidLocatorQRCode: 'Lagerort QR ungültig',
      invalidQtyToMove: 'Bewegungsmenge ungültig',
    },
    confirmButton: {
      default: {
        caption: 'Bestätigen',
        promptQuestion: 'Bist du sicher?',
        yes: 'Ja',
        no: 'Nein',
      },
      abort: 'Rückgängig',
      notFound: 'Nicht gefunden',
    },
    mfg: {
      ProductName: 'Produkt',
      target: 'Soll',
      picked: 'Ist',
      generateHUQRCodes: {
        packing: 'Verpackung',
        qtyTUs: 'Anzahl TUs',
        print: 'Drucken',
        numberOfHUs: 'Anzahl der Gebinde',
        numberOfCopies: 'Kopien',
      },
      issues: {
        target: 'Zuf. Soll',
        picked: 'Ist',
        qtyToIssueTarget: 'Menge Soll',
        qtyToIssueRemaining: 'noch offen',
        qtyIssued: 'Menge Ist',
        qtyRejected: 'Menge verworfen',
        step: {
          name: 'HU einfüllen',
        },
      },
      receipts: {
        qtyToReceiveTarget: 'Sollmenge',
        qtyReceived: 'Produziert',
        qtyToReceive: 'noch offen',
        btnReceiveTarget: 'Gebinde',
        btnReceiveProducts: 'Produzieren',
        existingLU: 'Scan',
        newHU: 'Neues Gebinde',
        target: 'Empf. Soll',
        picked: 'Ist',
      },
    },
  },
};

export default translations;
