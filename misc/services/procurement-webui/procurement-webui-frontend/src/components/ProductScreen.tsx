import React, { useContext, useEffect } from 'react';
import { observer } from 'mobx-react';
import { getSnapshot } from 'mobx-state-tree';
import { useParams, useHistory } from 'react-router-dom';
import { formDate, prettyDate } from '../utils/date';
import DailyNav from './DailyNav';
import View from './View';
import { RootStoreContext } from '../models/Store';

interface RouteParams {
  productId?: string;
  targetDay?: string;
  targetDayCaption?: string;
}

const ProductScreen: React.FunctionComponent = observer(() => {
  const { productId, targetDay, targetDayCaption } = useParams<RouteParams>();
  const store = useContext(RootStoreContext);
  const products = getSnapshot(store.dailyProducts.products);
  const product = products.find((prod) => prod.productId === productId);
  const { lang } = store.i18n;
  const currentDay = targetDay ? targetDay : store.app.currentDay;
  const currentCaption = targetDayCaption ? targetDayCaption : store.app.dayCaption;
  const qtyInput = React.createRef<HTMLInputElement>();

  const selectAndFocus = () => {
    if (qtyInput.current) {
      qtyInput.current.focus();
      qtyInput.current.select();
    }
  };

  useEffect(() => {
    store.navigation.setViewNames(product.productName);
    selectAndFocus();
  }, [store]);

  const saveQty = (newQty: number) => {
    store
      .postDailyReport({
        items: [
          {
            date: currentDay,
            productId: product.productId,
            qty: newQty ? newQty : 0,
          },
        ],
      })
      .then(() => {
        store.fetchDailyReport(currentDay);
        store.app.getUserSession();
      });
  };
  const history = useHistory();
  const { navigation } = store;
  const productQty = product.qty.toString();

  return (
    <View>
      <div>
        <DailyNav
          isStatic={true}
          staticDay={prettyDate({ lang, date: formDate({ currentDay: new Date(currentDay) }) })}
          staticCaption={currentCaption}
        />
        <div className="mt-5 p-4">
          <div className="columns is-mobile">
            <div className="column is-11">
              <input
                className="product-input"
                type="number"
                onKeyUp={(e) => {
                  if (e.key === 'Enter') {
                    qtyInput.current.blur();
                    navigation.removeViewFromHistory();
                    history.goBack();
                  }
                }}
                ref={qtyInput}
                step="1"
                value={productQty.length > 1 ? productQty.replace(/^0+/, '') : productQty}
                onChange={(e) => {
                  let updatedQty = parseInt(e.target.value, 10);
                  updatedQty = isNaN(updatedQty) ? 0 : updatedQty;
                  store.dailyProducts.updateProductQty(product.productId, updatedQty);
                }}
                onBlur={(e) => saveQty(parseInt(e.target.value, 10))}
              />
            </div>
            {/* The arrows */}
            <div className="columns pt-4 green-color">
              <div
                className="column is-6"
                onClick={() => {
                  saveQty(parseInt(qtyInput.current.value, 10) + 1);
                }}
              >
                <i className="fas fa-2x fa-arrow-up"></i>
              </div>
              <div
                className="column is-6"
                onClick={() => {
                  const currentQty = parseInt(qtyInput.current.value, 10);
                  currentQty > 0 && saveQty(currentQty - 1);
                }}
              >
                <span>
                  <i className="fas fa-2x fa-arrow-down"></i>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </View>
  );
});

export default ProductScreen;
