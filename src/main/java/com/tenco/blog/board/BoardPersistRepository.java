package com.tenco.blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor // 필수 멤버변수를 확인해서 생성자에 등록해줌
@Repository // Ioc 대상이 된다 - 싱글톤 패턴으로 관리 됨
public class BoardPersistRepository {

    // JPA 핵심 인터페이스
    // @Autowired  final 를 사용시 사용 불가
    private final EntityManager em;

    // 게시글 한건 조회 쿼리 만들기
    // em.find(), JPQL, 네이티브 쿼리
    public Board findById(Long id) {
        // 1차 캐시 활용
        return em.find(Board.class, id);
    }

    // JQPL를 사용환 조회 방법 (비교용 - 실제로는 find() 권장)
    public Board findByIdWithJPQL(Long id) {
        // 네임드 파라미터 권장 사용
        String jpql = " SELECT b FROM Board b WHERE b.id = :id ";
//        Query query = em.createQuery(jpql, Board.class);
//        query.setParameter("id", id);
//        Board board = (Board) query.getSingleResult();
        try {
            return em.createQuery(jpql, Board.class)
                    .setParameter("id", id)
                    .getSingleResult(); // 주의점 : 결과 없으면 NoResultException 발생
        } catch (Exception e) {
            return null;
        }

        // JPQL 단점 :
        // 1. 1차 캐시 우회하여 항상 DB 접근
        // 2. 코드가 복잡하게 나올 수 있음
        // 3. getSingleResult() 호출 <-- 예외 처리 해주어야 함
    }





    // JPQL을 사용한 게시글 목록 조회
    public List<Board> findAll() {
        // JPQL : 엔티티 객체를 대상으로 하는 객체지향 쿼리
        // Board는 엔티티 클르명, b는 별칭
        String jpql = " SELECT b FROM Board b ORDER BY b.id DESC ";

        // v1
        //em.createNativeQuery()
//        Query query = em.createQuery(jpql, Board.class);
//        List<Board> boardList = query.getResultList();
//        return boardList;
        return em.createQuery(jpql, Board.class).getResultList();
    }


    // 게시글 저장 기능 - 영속성 컨텍스트 활용
    @Transactional
    public Board save(Board board) {
        // v1 -> 네이트브 쿼리를 활용 했다.

        // 1. 매개변수로 받은 board는 현재 비영속 상태이다.
        //     - 아직 영속성 컨텍스트에 관리 되지 않는 상태
        //     - 데이터베이스와 아직은 연관 없는 순수 Java 객체 상태

        // 2. em.persist(board); - 이 엔티티를 영속성 컨텍스트에 저장하는 개념이다.
        //     - 영속성 컨텍스트가 board 객체를 관리하게 된다.
        em.persist(board);

        // 3. 트랜잭션 커밋 시점에 Insert 쿼리 실행
        //     - 이때 영속성 컨텍스트의 변경 사항이 자동으로 DB에 반영 됨
        //     - board 객체의 id 필드에 자동으로 생성된 값이 설정 됨
        // insert ---> DB ---> (pk 값을 알 수 있다)
        // select ---> DB ---> (할당된 pk 값 조회)

        // 4. 영속 상태로 된 board 객체를 반환
        //     - 이 시점에는 자동으로 board id 멤버 변수에 db pk 값이 할당된 상태이다
        return board;
    }
}
