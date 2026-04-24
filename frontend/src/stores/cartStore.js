import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const useCartStore = defineStore("cart", () => {
  // =========================================================================
  // State
  // =========================================================================

  // Cart items
  const items = ref([]);

  // =========================================================================
  // Getters
  // =========================================================================

  // Total item count
  const totalItems = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  );

  // Total price
  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  );

  // Is the cart empty or not?
  const isEmpty = computed(() => items.value.length === 0);

  // Group items by store — for display and checkout
  const cartByStore = computed(() => {
    const grouped = {};
    items.value.forEach((item) => {
      if (!grouped[item.storeId]) {
        grouped[item.storeId] = {
          storeId: item.storeId,
          storeName: item.storeName,
          storeSlug: item.storeSlug,
          items: [],
          subtotal: 0,
        };
      }
      grouped[item.storeId].items.push(item);
      grouped[item.storeId].subtotal += item.price * item.quantity;
    });
    return grouped;
  });

  // List store groups sebagai array
  const cartStores = computed(() => Object.values(cartByStore.value));

  // =========================================================================
  // Actions
  // =========================================================================

  // Add item to cart
  function addItem(product, store) {
    const existing = items.value.find(
      (i) => i.productId === product.id && i.storeId === store.id
    );

    if (existing) {
      existing.quantity += 1;
    } else {
      items.value.push({
        productId: product.id,
        sku: product.sku,
        name: product.name,
        price: product.price,
        quantity: 1,
        storeId: store.id,
        storeName: store.name,
        storeSlug: store.slug,
      });
    }
  }

  // Update quantity item
  function updateQuantity(productId, storeId, quantity) {
    const item = items.value.find(
      (i) => i.productId === productId && i.storeId === storeId
    );
    if (!item) return;

    if (quantity <= 0) {
      removeItem(productId, storeId);
    } else {
      item.quantity = quantity;
    }
  }

  // Remove an item from the cart
  function removeItem(productId, storeId) {
    items.value = items.value.filter(
      (i) => !(i.productId === productId && i.storeId === storeId)
    );
  }

  // Remove all items from a specific store
  function removeStoreItems(storeId) {
    items.value = items.value.filter((i) => i.storeId !== storeId);
  }

  // Clear cart
  function clearCart() {
    items.value = [];
  }

  // Check if the product is already in the cart
  function isInCart(productId, storeId) {
    return items.value.some(
      (i) => i.productId === productId && i.storeId === storeId
    );
  }

  // Select the product quantity in the cart
  function getQuantity(productId, storeId) {
    const item = items.value.find(
      (i) => i.productId === productId && i.storeId === storeId
    );
    return item?.quantity || 0;
  }

  return {
    // State
    items,

    // Getters
    totalItems,
    totalPrice,
    isEmpty,
    cartByStore,
    cartStores,

    // Actions
    addItem,
    updateQuantity,
    removeItem,
    removeStoreItems,
    clearCart,
    isInCart,
    getQuantity,
  };
});