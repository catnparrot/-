# 백업용


ALTER table k_member
ADD CONSTRAINT manager CHECK (cash>=0);

ALTER table product
ADD CONSTRAINT manager2 CHECK (quantity>=0);
