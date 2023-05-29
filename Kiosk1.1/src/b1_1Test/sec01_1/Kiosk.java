package b1_1Test.sec01_1;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import b1_1Test.sec02.Admin;

// Kiosk.java


public class Kiosk extends DBConnector {
    private DBConnector dbConnector;
    private Scanner scanner;
    private int loginAttempt;
    private String loggedInUserId;

    public Kiosk() {
        dbConnector = new DBConnector();
        scanner = new Scanner(System.in);
        loginAttempt = 0;
    }

    public void start() {
        while (true) {
            System.out.println("========== Kiosk ==========");
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("0. 종료");
            System.out.print("메뉴 선택: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
            	adminMode();
            	break;
            case 0:
                System.out.println("프로그램을 종료합니다.");
                return;
            default:
                System.out.println("잘못된 메뉴 선택입니다.");
            }
        }
    }
    
    public void login() {
        loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false; // 로그인 여부

        while (!loggedIn && loginAttempt < 3) {
            KMember kmember = new KMember();
            System.out.println("[로그인]");
            System.out.print("아이디: ");
            kmember.setId(scanner.nextLine());
            System.out.print("비밀번호: ");
            kmember.setPassword(scanner.nextLine());

            try {
                String query = ""
                		+ "SELECT password FROM k_member"
                		+ " WHERE id= ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, kmember.getId());
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(kmember.getPassword())) {
                        loggedIn = true; // 로그인 성공
                        loggedInUserId = kmember.getId();
                    } else {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                    }
                } else {
                    System.out.println("아이디가 존재하지 않습니다.");
                }
                rs.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
            	if(loggedInUserId != null) {
            	System.out.println("어서오세요, "+loggedInUserId+"님. 무엇을 도와드릴까요?");		//loginId 값 부여 확인용
            	}
            }
            loginAttempt++; // 로그인 시도 횟수 증가
        }

        if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }
        showMenu();
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n========== 메뉴 ==========");
            System.out.println("1. 물품 구매");
            System.out.println("2. 재고 확인");
            System.out.println("3. 현금 충전");
            System.out.println("0. 로그아웃");
            System.out.print("메뉴 선택: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            switch (choice) {
                case 1:
                    purchaseProduct();
                    break;
                case 2:
                    displayProducts();
                    break;
                case 3:
                    rechargeCash();
                    break;
                case 0:
                    System.out.println("로그아웃 되었습니다.");
                    return;
                default:
                    System.out.println("잘못된 메뉴 선택입니다.");
            }
        }
    }

    public void displayProducts() {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT * FROM product");
            System.out.println("========== 상품 목록 ==========");
            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("product_name");
                int price = resultSet.getInt("price");
                int quantity = resultSet.getInt("quantity");
                System.out.println("상품 ID: " + productId + ", 상품명: " + productName + ", 가격: " + price + ", 재고: " + quantity);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void purchaseProduct() {
        // Display all product information
        System.out.println("전체 상품 목록 및 재고:");
        displayProducts();

        System.out.println("구매할 품목 개수를 입력하세요:");
        int itemCount = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        List<Integer> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (int i = 0; i < itemCount; i++) {
            System.out.print("구매할 상품 ID를 입력하세요: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            System.out.print("구매 수량을 입력하세요: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            productIds.add(productId);
            quantities.add(quantity);
        }

        boolean allProductsAvailable = true;
        double totalPrice = 0.0;

        for (int i = 0; i < itemCount; i++) {
            int productId = productIds.get(i);
            int quantity = quantities.get(i);

            if (!isProductAvailable(productId, quantity)) {
                allProductsAvailable = false;
                break;
            }

            totalPrice += calculateTotalPrice(productId, quantity);
        }

        if (allProductsAvailable==true) {
            double userCash = getUserCash();

            if (userCash >= totalPrice) {
                boolean success = true;

                for (int i = 0; i < itemCount; i++) {
                    int productId = productIds.get(i);
                    int quantity = quantities.get(i);

                    if (!updateProductQuantity(productId, quantity)) {
                        success = false;
                        break;
                    }
                }

                if (success && updateUserCash(userCash - totalPrice)) {
                    System.out.println("상품을 구매하였습니다.");

                    // 영수증 출력
                    for (int i = 0; i < itemCount; i++) {
                        int productId = productIds.get(i);
                        int quantity = quantities.get(i);
                        double itemPrice = calculateTotalPrice(productId, quantity);
                        printReceipt(productId, quantity, itemPrice);
                    }

                    // 구매 후 상품 재고 확인
                    System.out.println("구매 후 상품 재고:");
                    for (int i = 0; i < itemCount; i++) {
                        int productId = productIds.get(i);
                        displayProductQuantity(productId);
                    }

                    // 보유 현금 확인
                    System.out.println("보유 현금: " + getUserCash());
                } else {
                    System.out.println("상품 구매에 실패했습니다.");
                }
            } else {
                System.out.println("보유 현금이 부족합니다.");
            }
        } else {
            System.out.println("상품의 재고가 부족합니다.");
        }
    }
    
    private void printReceipt(int productId, int quantity, double totalPrice) {
        // 현재 시간을 얻기 위한 날짜 포맷 지정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());

        System.out.println("\n========== 영수증 ==========");
        System.out.println("구매 상품 ID: " + productId);
        System.out.println("구매 수량: " + quantity);
        System.out.println("총 가격: " + totalPrice);
        System.out.println("구매 시간: " + currentTime);
        System.out.println("============================");
    }

    
    

    private void rechargeCash() {
        System.out.print("충전할 금액을 입력하세요: ");
        double cash = Double.parseDouble(scanner.nextLine());
        
        double userCash = getUserCash();
        if (updateUserCash(userCash + cash)) {
            System.out.println("현금을 충전하였습니다.");
        } else {
            System.out.println("현금 충전에 실패했습니다.");
        }
    }

    private boolean isProductAvailable(int productId, int quantity) {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT * FROM product WHERE product_id = " + productId + " AND quantity >= " + quantity);
            boolean isAvailable = resultSet.next();
            resultSet.close();
            return isAvailable;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private double calculateTotalPrice(int productId, int quantity) {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT price FROM product WHERE product_id = " + productId);
            if (resultSet.next()) {
                double price = resultSet.getDouble("price");
                resultSet.close();
                return price * quantity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getUserCash() {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT cash FROM k_member WHERE id = '" + loggedInUserId + "'");
            if (resultSet.next()) {
                double cash = resultSet.getDouble("cash");
                resultSet.close();
                return cash;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private boolean updateProductQuantity(int productId, int quantity) {
        try {
            int updatedRows = dbConnector.executeUpdate("UPDATE product SET quantity = quantity - " + quantity + " WHERE product_id = " + productId);
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateUserCash(double cash) {
        try {
            PreparedStatement statement = dbConnector.getConnection().prepareStatement("UPDATE k_member SET cash = ? WHERE id = ?");
            statement.setDouble(1, cash);
            statement.setString(2, loggedInUserId);
            int updatedRows = statement.executeUpdate();
            statement.close();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void register() {
        System.out.print("사용할 아이디를 입력하세요: ");
        String id = scanner.nextLine();
        System.out.print("사용할 비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        if (isIdAvailable(id)) {
            if (createUser(id, password)) {
                System.out.println("회원가입이 완료되었습니다. 로그인해주세요.");
            } else {
                System.out.println("회원가입에 실패했습니다.");
            }
        } else {
            System.out.println("이미 사용 중인 아이디입니다. 다른 아이디를 선택해주세요.");
        }
    }
    
    private boolean isIdAvailable(String id) {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT * FROM k_member WHERE id = '" + id + "'");
            boolean isAvailable = !resultSet.next(); // 이미 존재하는 경우 false 반환
            resultSet.close();
            return isAvailable;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean createUser(String id, String password) {
        try {
            PreparedStatement statement = dbConnector.getConnection().prepareStatement("INSERT INTO k_member (id, password, cash) VALUES (?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, password);
            statement.setDouble(3, 0.0); // 초기 보유 현금은 0으로 설정
            int insertedRows = statement.executeUpdate();
            statement.close();
            return insertedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void displayProductQuantity(int productId) {
        try {
            ResultSet resultSet = dbConnector.executeQuery("SELECT quantity FROM product WHERE product_id = " + productId);
            if (resultSet.next()) {
                int quantity = resultSet.getInt("quantity");
                System.out.println("상품 ID: " + productId + ", 재고: " + quantity);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    


    private void adminMode() {
    	loginAttempt = 0; // 로그인 시도 횟수
        boolean loggedIn = false;
    	
        while(!loggedIn && loginAttempt < 3) {
        	Admin admin = new Admin();
        	System.out.print("관리자 아이디를 입력하세요: ");
        	admin.setAdId(scanner.nextLine());
        	System.out.print("관리자 비밀번호를 입력하세요.: ");
        	admin.setAdPassword(scanner.nextLine());
        	
        	try {
				String query = ""
						+ "SELECT password FROM admin"
						+ " WHERE id= ?";
				PreparedStatement statement=connection.prepareStatement(query);
				statement.setString(1, admin.getAdId());
				ResultSet resultSet=statement.executeQuery();
				if(resultSet.next()) {
					String dbPassword = resultSet.getString("password");
                    if (dbPassword.equals(admin.getAdPassword())) {
                        loggedIn = true; // 로그인 성공
                        loggedInUserId = admin.getAdId();
                        System.out.println("관리자 모드로 전환되었습니다.");
                        
                        while (true) {
                            System.out.println("\n========== 관리자 모드 ==========");
                            System.out.println("1. 재고 채우기");
                            System.out.println("2. 재고 확인");
                            System.out.println("0. 돌아가기");
                            System.out.print("메뉴 선택: ");
                            int choice = scanner.nextInt();
                            scanner.nextLine(); // 버퍼 비우기

                            switch (choice) {
                                case 1:
                                    fillStock();
                                    break;
                                case 2:
                                    displayProducts();
                                    break;
                                case 0:
                                    System.out.println("관리자 모드를 종료합니다.");
                                    start();
                                    break;
                                default:
                                    System.out.println("잘못된 메뉴 선택입니다.");
                            }
                        }        
                    } else {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                    }
                } else {
                    System.out.println("아이디가 존재하지 않습니다.");
                }
				
				
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("관리자 시스템에 문제가 발생하여 관리자 모드를 종료합니다.");
				start();
			}
        	
            loginAttempt++; // 로그인 시도 횟수 증가
        }
        
    	if (!loggedIn) {
            start(); // 3번 연속 실패시 start() 메서드 호출
        }
    }


    private void fillStock() {
        System.out.print("추가할 상품 ID를 입력하세요: ");
        int productId = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        System.out.print("추가할 수량을 입력하세요: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        try {
            PreparedStatement statement = dbConnector.getConnection().prepareStatement("UPDATE product SET quantity = quantity + ? WHERE product_id = ?");
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            int rowsUpdated = statement.executeUpdate();
            statement.close();
            if (rowsUpdated > 0) {
                System.out.println("재고를 추가하였습니다.");
            } else {
                System.out.println("상품 ID를 확인하세요.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
