# Phân tích logic kịch bản phức tạp trong ShoppingCartService

## Kịch bản thực tế

### Bước 1

Người dùng A thêm sản phẩm X vào giỏ hàng:

```text
Sản phẩm X:
- Giá: 500.000 VND
- Tồn kho hiện tại: 10
```

Người dùng A thêm:

```text
5 sản phẩm X
```

Kết quả:

```text
Giỏ hàng A:
- Product X: 5
```

Tồn kho hệ thống lúc này vẫn là:

```text
10
```

(vì chỉ mới thêm vào giỏ, chưa checkout)

---

## Bước 2

Ngay sau đó, người dùng B mua thành công:

```text
3 sản phẩm X
```

Hệ thống cập nhật tồn kho:

```text
10 - 3 = 7
```

Tồn kho hiện tại của sản phẩm X:

```text
7
```

---

## Bước 3

Người dùng A quay lại giỏ hàng và cập nhật:

```text
5 -> 7 sản phẩm
```

Hệ thống cần kiểm tra:

```text
tồn kho hiện tại = 7
```

Nếu giỏ hàng A đã giữ:

```text
5
```

thì việc tăng lên:

```text
7
```

nghĩa là cần thêm:

```text
2 sản phẩm nữa
```

Hệ thống phải xác minh:

```text
7 sản phẩm yêu cầu <= 7 sản phẩm tồn kho hiện tại
```

Nếu không xử lý đúng sẽ phát sinh lỗi.

---

# Các điểm tiềm ẩn gây lỗi trong ShoppingCartService

---

# 1. Dữ liệu tồn kho bị stale (không đồng bộ)

## Nguyên nhân

ShoppingCartService có thể đang dùng dữ liệu tồn kho cũ được cache hoặc lấy từ trước đó.

Ví dụ:

- Người dùng A thêm hàng khi stock = 10
- Sau đó stock giảm còn 7
- Nhưng service vẫn kiểm tra theo stock cũ = 10

---

## Hậu quả

Hệ thống cho phép:

```text
A cập nhật lên 9 sản phẩm
```

mặc dù tồn kho thật chỉ còn:

```text
7
```

---

## Lỗi nghiệp vụ

Vi phạm quy tắc:

> Người dùng chỉ được thêm/cập nhật nếu tồn kho đủ.

---

## Hành vi không mong muốn

- Overselling
- Checkout thất bại
- Dữ liệu giỏ hàng sai
- Trải nghiệm người dùng kém

---

# 2. Không kiểm tra tồn kho khi update quantity

## Nguyên nhân

Một số hệ thống chỉ kiểm tra tồn kho lúc add-to-cart nhưng quên kiểm tra khi update quantity.

Ví dụ:

```text
addProductToCart() -> có validate stock
updateProductQuantity() -> không validate
```

---

## Hậu quả

Người dùng có thể:

```text
5 -> 100 sản phẩm
```

mặc dù tồn kho chỉ còn:

```text
7
```

---

## Ngoại lệ có thể xảy ra

- IllegalArgumentException
- IllegalStateException
- BusinessException: "Insufficient stock"

Nếu không xử lý:

- database inconsistency
- checkout fail
- âm tồn kho

---

# 3. Race Condition khi nhiều người dùng thao tác đồng thời

## Nguyên nhân

Hai người dùng cùng cập nhật sản phẩm gần như cùng lúc.

Ví dụ:

| User   | Action              |
|--------|---------------------|
| A      | update quantity = 7 |
| B      | mua thêm 5 sản phẩm |
| System | xử lý song song     |

---

## Hậu quả

Cả hai transaction cùng pass kiểm tra tồn kho.

Kết quả cuối:

```text
Tồn kho âm
```

hoặc:

```text
số lượng bán > tồn kho thật
```

---

## Đây là lỗi rất nguy hiểm

Có thể gây:

- mất doanh thu
- sai dữ liệu inventory
- hoàn tiền thủ công
- khiếu nại khách hàng

---

# 4. Giá sản phẩm không được cập nhật theo giá hiện hành

## Quy tắc nghiệp vụ

> Giá trong giỏ phải luôn dùng giá hiện hành.

---

## Tình huống lỗi

Người dùng thêm sản phẩm khi giá:

```text
500.000
```

Sau đó admin cập nhật:

```text
600.000
```

Nhưng ShoppingCartService vẫn dùng giá cũ.

---

## Hậu quả

- Tổng tiền sai
- Thanh toán sai
- Thiệt hại doanh thu

---

# 5. Product bị xóa khỏi hệ thống nhưng vẫn còn trong cart

## Tình huống

Admin xóa sản phẩm X khỏi hệ thống.

Nhưng trong giỏ A vẫn còn:

```text
Product X
```

---

## Nếu service không xử lý tốt

Khi update/remove cart item:

```java
productRepository.findById(productId)
```

trả về:

```java
Optional.empty()
```

---

## Hậu quả

Có thể xảy ra:

```text
NullPointerException
```

hoặc:

```text
NoSuchElementException
```

---

## Hành vi đúng

Hệ thống nên:

- cho phép remove item an toàn
- hoặc thông báo:

```text
"Product no longer exists"
```

---

# 6. Không validate quantity > 0

## Tình huống

Người dùng gửi:

```text
quantity = 0
```

hoặc:

```text
quantity = -5
```

---

## Nếu không validate

Có thể gây:

- cart item âm
- total price âm
- lỗi logic thanh toán

---

# Kết luận

ShoppingCartService là module có rủi ro cao vì:

- phụ thuộc dữ liệu tồn kho realtime
- nhiều người dùng thao tác đồng thời
- liên quan trực tiếp tới doanh thu

Nếu không được thiết kế và kiểm thử kỹ:

- có thể gây overselling
- dữ liệu cart sai
- lỗi checkout
- tồn kho âm
- giá thanh toán không chính xác
- crash hệ thống do dữ liệu không đồng bộ

Do đó cần:

- kiểm thử đầy đủ Happy Path và Unhappy Path
- mock chính xác ProductRepository và CartRepository
- verify interaction bằng Mockito
- kiểm tra các edge cases và concurrency scenarios cẩn thận.